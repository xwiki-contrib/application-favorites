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
package org.xwiki.contrib.favorites.script;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.script.service.ScriptService;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Favorites Script service.
 * @since 1.4.0
 * @version $Id$
 */
@Component
@Singleton
public class FavoritesScriptService implements ScriptService
{
    private static final LocalDocumentReference FAVORITES_CLASS_REF =
        new LocalDocumentReference(Arrays.asList("Favorites", "Code"), "FavoritesClass");

    private static final String PAGES = "pages";

    private static final String FAVORITES_USER_UPDATE_PROFILE = "favorites.user.updateProfile";
    private static final String DONE = "done";
    private static final String WARNING = "warning";

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Inject
    private ContextualLocalizationManager l10n;

    @Inject
    @Named("compactwiki")
    private EntityReferenceSerializer<String> compactWikiSerializer;

    /**
     * @param userReference the user for which to add the favorite
     * @param docRef the entity to favorite (for now, only documents)
     * @return a map with a success and a status members, for JSON serialization
     */
    public Map<String, String> add(DocumentReference userReference, DocumentReference docRef) throws XWikiException
    {
        XWikiContext context = xcontextProvider.get();
        XWiki xwiki = context.getWiki();
        XWikiDocument userDoc = xwiki.getDocument(userReference, context);

        if (!xwiki.exists(docRef, context) || userDoc.isNew()) {
            return getStatusObject(l10n.getTranslationPlain("favorites.user.addCurrentPage.fail"), "error");
        }

        // Create the favorites object if it does not exist.
        BaseObject favObj = userDoc.getXObject(FAVORITES_CLASS_REF, true, context);

        List<String> favDocs = favObj.getListValue(PAGES);
        if (favDocs == null) {
            // Initialize the list of favorite documents upon object creation.
            favDocs = new ArrayList<>();
        }

        String favDocFullName = compactWikiSerializer.serialize(docRef, userReference);
        if (favDocs.contains(favDocFullName)) {
            // add the reference only if it 's not already stored
            return getStatusObject(l10n.getTranslationPlain("favorites.user.addCurrentPage.alreadyExists"), WARNING);
        }

        favDocs.add(favDocFullName);
        favObj.setStringListValue(PAGES, favDocs);
        xwiki.saveDocument(userDoc, l10n.getTranslationPlain(FAVORITES_USER_UPDATE_PROFILE), context);
        return getStatusObject(l10n.getTranslationPlain("favorites.user.addCurrentPage.success"), DONE);
    }

    /**
     * @param userReference the user for which to remove the favorite
     * @param docRef the entity to unfavorite (for now, only documents)
     * @return a map with a success and a status members, for JSON serialization
     */
    public Map<String, String> remove(DocumentReference userReference, DocumentReference docRef) throws XWikiException
    {
        XWikiContext context = xcontextProvider.get();
        XWiki xwiki = context.getWiki();
        XWikiDocument userDoc = xwiki.getDocument(userReference, context);
        BaseObject favObj = userDoc.getXObject(FAVORITES_CLASS_REF, false, context);
        String favDocFullName = compactWikiSerializer.serialize(docRef, userReference);
        List<String> favDocs = favObj == null ? null : favObj.getListValue(PAGES);
        int favIndex = favDocs == null ? -1 : favDocs.indexOf(favDocFullName);
        if (favIndex == -1) {
            return getStatusObject(l10n.getTranslationPlain("favorites.user.removeCurrentPage.doesntExist"), WARNING);
        }

        favDocs.remove(favIndex);
        favObj.set(PAGES, favDocs, context);
        xwiki.saveDocument(userDoc, l10n.getTranslationPlain(FAVORITES_USER_UPDATE_PROFILE), context);
        return getStatusObject(l10n.getTranslationPlain("favorites.user.removeCurrentPage.success"), DONE);
    }

    private static Map<String, String> getStatusObject(String message, String status)
    {
        Map<String, String> s = new LinkedHashMap<>(2);
        s.put("message", message);
        s.put("status", status);
        return s;
    }
}
