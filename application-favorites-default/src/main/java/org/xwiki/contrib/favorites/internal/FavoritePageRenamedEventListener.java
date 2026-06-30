/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.favorites.internal;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CursorMarkParams;
import org.slf4j.Logger;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.favorites.FavoriteManager;
import org.xwiki.contrib.favorites.FavoritesException;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.observation.ObservationContext;
import org.xwiki.observation.event.AbstractLocalEventListener;
import org.xwiki.observation.event.Event;
import org.xwiki.refactoring.event.DocumentRenamedEvent;
import org.xwiki.refactoring.event.DocumentRenamingEvent;
import org.xwiki.search.solr.Solr;
import org.xwiki.search.solr.SolrException;
import org.xwiki.search.solr.SolrUtils;
import org.xwiki.search.solr.internal.SolrClientInstance;
import org.xwiki.search.solr.internal.api.FieldUtils;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Listener dedicated to update the document reference of the favorites when a page is renamed or moved.
 *
 * @version $Id$
 * @since 1.4.4
 */
@Component
@Singleton
@Named(FavoritePageRenamedEventListener.NAME)
public class FavoritePageRenamedEventListener extends AbstractLocalEventListener
{
    /**
     * The listener name.
     */
    public static final String NAME = "FavoritePageRenamedEventListener";

    private static final DocumentRenamingEvent DOCUMENT_RENAMING_EVENT = new DocumentRenamingEvent();

    private static final LocalDocumentReference FAVORITES_CLASS_REF = new LocalDocumentReference(
        Arrays.asList("Favorites", "Code"), "FavoritesClass");

    private static final String PAGES = "pages";

    private static final int ROWS = 1000;

    @Inject
    private FavoriteManager favoriteManager;

    @Inject
    private Solr solr;

    @Inject
    private SolrUtils solrUtils;

    @Inject
    private DocumentReferenceResolver<SolrDocument> solrDocumentReferenceResolver;

    @Inject
    @Named("local")
    private EntityReferenceSerializer<String> localReferenceSerializer;

    @Inject
    private EntityReferenceSerializer<String> globalReferenceSerializer;

    @Inject
    @Named("compactwiki")
    private EntityReferenceSerializer<String> compactWikiSerializer;

    @Inject
    private Logger logger;

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Inject
    private ObservationContext observationContext;

    /**
     * Constructor.
     */
    public FavoritePageRenamedEventListener()
    {
        super(NAME, new DocumentRenamedEvent(), new DocumentDeletedEvent());
    }

    @Override
    public void processLocalEvent(Event event, Object source, Object data)
    {
        if (event instanceof DocumentDeletedEvent && this.observationContext.isIn(DOCUMENT_RENAMING_EVENT)) {
            // A delete event is triggered before each document rename event, but we don't want to remove the favorite
            // page in this case because we won't be able to replace the favorite page later when the rename
            // event is triggered. For this reason we have to check if this is really a delete and not a rename.
            return;
        }

        DocumentReference originalRef;
        if (event instanceof DocumentRenamedEvent) {
            DocumentRenamedEvent documentRenamedEvent = (DocumentRenamedEvent) event;
            originalRef = documentRenamedEvent.getSourceReference();
        } else {
            XWikiDocument doc = (XWikiDocument) source;
            originalRef = doc.getDocumentReference();
        }
        cleanOrUpdateFavorite(event, originalRef);
    }

    private void cleanOrUpdateFavorite(Event event, DocumentReference originalRef)
    {
        String serializedPrevRef = localReferenceSerializer.serialize(originalRef);
        String globalSerializedRef = globalReferenceSerializer.serialize(originalRef);

        // We need to query on the 2 version of the reference, one with the XWiki prefix and the one without it.
        // As favorite is multi wiki we store the wiki prefix for the pages page that is not in the same wiki than the
        // user. Note this query can return false positive (as we can have the same relative reference in another wiki)
        // that's why we check, before removing or updating the favorite, if the full reference is in the favorite of
        // the user.
        String solrQueryStr =
            "property.Favorites.Code.FavoritesClass.pages:"
                + solrUtils.toFilterQueryString(serializedPrevRef)
                + " OR property.Favorites.Code.FavoritesClass.pages:"
                + solrUtils.toFilterQueryString(globalSerializedRef);

        try {
            SolrQuery solrQuery = new SolrQuery(solrQueryStr);
            solrQuery.setRows(ROWS);
            // Set sorting based on the ID for cursor-based pagination to work.
            solrQuery.addSort(FieldUtils.ID, SolrQuery.ORDER.asc);
            solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, CursorMarkParams.CURSOR_MARK_START);

            QueryResponse response;
            do {
                response = solr.getClient(SolrClientInstance.CORE_NAME).query(solrQuery);
                SolrDocumentList solrDocuments = response.getResults();
                for (SolrDocument d : solrDocuments) {
                    DocumentReference docRef = solrDocumentReferenceResolver.resolve(d);
                    if (favoriteManager.has(docRef, originalRef)) {
                        if (event instanceof DocumentRenamedEvent) {
                            DocumentRenamedEvent documentRenamedEvent = (DocumentRenamedEvent) event;
                            updateFavorite(docRef, originalRef,
                                documentRenamedEvent.getTargetReference());
                        } else if (event instanceof DocumentDeletedEvent) {
                            removeFavorite(docRef, originalRef);
                        }
                    }
                }
                solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, response.getNextCursorMark());
            } while (response.getResults().size() == ROWS);
        } catch (XWikiException | FavoritesException e) {
            logger.error("Error while updating favorites.", e);
        } catch (SolrException | SolrServerException | IOException e) {
            logger.error("Error while quering impacted favorites from Solr.", e);
        }
    }

    private void removeFavorite(EntityReference userRef, EntityReference prevRef)
        throws XWikiException
    {
        XWikiContext context = xcontextProvider.get();
        XWiki xwiki = context.getWiki();

        XWikiDocument userDoc = xwiki.getDocument(userRef, context).clone();
        BaseObject favObj = userDoc.getXObject(FAVORITES_CLASS_REF, true, context);

        List<String> favDocs = favObj.getListValue(PAGES);
        if (favDocs == null) {
            return;
        }
        boolean removed = favDocs.remove(compactWikiSerializer.serialize(prevRef, userRef));
        if (!removed) {
            return;
        }

        favObj.setDBStringListValue(PAGES, favDocs);
        xwiki.saveDocument(userDoc, "Remove favorite for deleted document.", context);
    }

    private void updateFavorite(EntityReference userRef, EntityReference prevRef, EntityReference newRef)
        throws XWikiException
    {
        XWikiContext context = xcontextProvider.get();
        XWiki xwiki = context.getWiki();

        XWikiDocument userDoc = xwiki.getDocument(userRef, context).clone();
        BaseObject favObj = userDoc.getXObject(FAVORITES_CLASS_REF, true, context);

        List<String> favDocs = favObj.getListValue(PAGES);
        if (favDocs == null) {
            return;
        }
        boolean removed = favDocs.remove(compactWikiSerializer.serialize(prevRef, userRef));
        if (!removed) {
            return;
        }
        favDocs.add(compactWikiSerializer.serialize(newRef, userRef));

        favObj.setDBStringListValue(PAGES, favDocs);
        xwiki.saveDocument(userDoc, "Update document after refactoring.", context);
    }
}
