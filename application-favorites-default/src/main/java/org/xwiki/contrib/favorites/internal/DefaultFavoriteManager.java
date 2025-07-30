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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.favorites.FavoriteManager;
import org.xwiki.contrib.favorites.FavoritesException;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.user.UserReference;
import org.xwiki.user.UserReferenceSerializer;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.api.Object;

/**
 * Default Favorites Manager implementation.
 * @since 1.4.0
 * @version $Id$
 */
@Component
@Singleton
public class DefaultFavoriteManager implements FavoriteManager
{
    private static final String FAVORITES_CLASS_REF = "Favorites.Code.FavoritesClass";

    private static final String PAGES = "pages";

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Inject
    @Named("compactwiki")
    private EntityReferenceSerializer<String> compactWikiSerializer;

    @Inject
    @Named("document")
    private UserReferenceSerializer<DocumentReference> userReferenceSerializer;

    @Override
    public boolean add(EntityReference userRef, EntityReference docRef) throws FavoritesException
    {
        return addAll(userRef, Collections.singletonList(docRef), "Added a favorite");
    }

    @Override
    public boolean add(UserReference userRef, EntityReference docRef) throws FavoritesException
    {
        return add(userReferenceSerializer.serialize(userRef), docRef);
    }

    @Override
    public boolean remove(EntityReference userRef, EntityReference docRef) throws FavoritesException
    {
        return removeAll(userRef, Collections.singletonList(docRef), "Removed a favorite");
    }

    @Override
    public boolean remove(UserReference userRef, EntityReference docRef) throws FavoritesException
    {
        return remove(userReferenceSerializer.serialize(userRef), docRef);
    }

    @Override
    public boolean addAll(EntityReference userRef, Collection<EntityReference> docRefs)
        throws FavoritesException
    {
        return addAll(userRef, docRefs, "Added favorites");
    }

    @Override
    public boolean addAll(UserReference userRef, Collection<EntityReference> docRefs) throws FavoritesException
    {
        return addAll(userReferenceSerializer.serialize(userRef), docRefs);
    }

    @Override
    public boolean addAll(EntityReference userRef, Collection<EntityReference> docRefs, String saveComment)
        throws FavoritesException
    {
        try {
            XWikiContext context = xcontextProvider.get();
            XWiki xwiki = context.getWiki();
            Document userDoc = new Document(xwiki.getDocument(userRef, context), context);
            checkUserExists(userRef, userDoc);

            // Create the favorites object if it does not exist.
            Object favObj = userDoc.getObject(FAVORITES_CLASS_REF, true);

            List<String> favDocs = (List<String>) favObj.getValue(PAGES);
            if (favDocs == null) {
                // Initialize the list of favorite documents upon object creation.
                favDocs = new ArrayList<>();
            }

            boolean updated = false;
            for (EntityReference docRef : docRefs) {
                String favDocFullName = compactWikiSerializer.serialize(docRef, userRef);
                if (favDocs.contains(favDocFullName)) {
                    // add the reference only if it 's not already stored
                    break;
                }
                updated = true;
                favDocs.add(favDocFullName);
            }

            if (updated) {
                favObj.set(PAGES, favDocs);
                userDoc.save(saveComment);
            }
            return updated;
        } catch (XWikiException e) {
            throw new FavoritesException(e);
        }
    }

    @Override
    public boolean addAll(UserReference userRef, Collection<EntityReference> docRefs, String saveComment)
        throws FavoritesException
    {
        return addAll(userReferenceSerializer.serialize(userRef), docRefs, saveComment);
    }

    @Override
    public boolean has(EntityReference userRef, EntityReference docRef) throws FavoritesException
    {
        try {
            XWikiContext context = xcontextProvider.get();
            XWiki xwiki = context.getWiki();
            Document userDoc = new Document(xwiki.getDocument(userRef, context), context);
            checkUserExists(userRef, userDoc);

            // Create the favorites object if it does not exist.
            Object favObj = userDoc.getObject(FAVORITES_CLASS_REF);
            List<String> favDocs = (List<String>) favObj.getValue(PAGES);
            if (favDocs == null) {
                return false;
            }
            String favDocFullName = compactWikiSerializer.serialize(docRef, userRef);
            return favDocs.contains(favDocFullName);
        } catch (XWikiException e) {
            throw new FavoritesException(e);
        }
    }

    @Override
    public boolean has(UserReference userRef, EntityReference docRef) throws FavoritesException
    {
        return false;
    }

    @Override
    public boolean removeAll(EntityReference userRef, Collection<EntityReference> docRefs)
        throws FavoritesException
    {
        return removeAll(userRef, docRefs, "Removed favorites");
    }

    @Override
    public boolean removeAll(EntityReference userRef, Collection<EntityReference> docRefs, String saveComment)
        throws FavoritesException
    {
        try {
            XWikiContext context = xcontextProvider.get();
            XWiki xwiki = context.getWiki();
            Document userDoc = new Document(xwiki.getDocument(userRef, context), context);
            Object favObj = userDoc.getObject(FAVORITES_CLASS_REF, false);
            List<String> favDocs = favObj == null ? null : (List<String>) favObj.getValue(PAGES);
            if (favDocs == null) {
                return false;
            }

            boolean updated = false;
            for (EntityReference docRef : docRefs) {
                String favDocFullName = compactWikiSerializer.serialize(docRef, userRef);
                updated = favDocs.remove(favDocFullName) || updated;
            }
            if (updated) {
                favObj.set(PAGES, favDocs);
                userDoc.save(saveComment);
            }
            return updated;
        } catch (XWikiException e) {
            throw new FavoritesException(e);
        }
    }

    @Override
    public boolean removeAll(UserReference userRef, Collection<EntityReference> docRefs, String saveComment)
        throws FavoritesException
    {
        return removeAll(userReferenceSerializer.serialize(userRef), docRefs, saveComment);
    }

    private static void checkUserExists(EntityReference userRef, Document userDoc) throws FavoritesException
    {
        if (userDoc.isNew()) {
            throw new FavoritesException(String.format("User [%s] does not exist", userRef));
        }
    }
}
