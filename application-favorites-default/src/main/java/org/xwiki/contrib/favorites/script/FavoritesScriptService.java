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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.favorites.FavoriteManager;
import org.xwiki.contrib.favorites.FavoritesException;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.xpn.xwiki.XWikiContext;

/**
 * Favorites Script service.
 * @since 1.4.0
 * @version $Id$
 */
@Component
@Singleton
@Named("favorites")
public class FavoritesScriptService implements ScriptService
{
    private static final String DONE = "done";
    private static final String WARNING = "warning";
    private static final String FAVORITES_USER_UPDATE_PROFILE = "favorites.user.updateProfile";

    private static final String ERROR = "error";
    private static final String FAVORITES_USER_ADD_CURRENT_PAGE_FAIL = "favorites.user.addCurrentPage.fail";

    @Inject
    private ContextualLocalizationManager l10n;

    @Inject
    private FavoriteManager favoriteManager;

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    /**
     * @param userRef the user for which to add the favorite
     * @param docRef the entity to favorite (for now, only documents)
     * @return a map with a success and a status members, for JSON serialization
     */
    public Map<String, String> add(DocumentReference userRef, DocumentReference docRef) throws FavoritesException
    {
        XWikiContext context = xcontextProvider.get();
        if (!context.getWiki().exists(docRef, context)) {
            return getStatus(l10n.getTranslationPlain(FAVORITES_USER_ADD_CURRENT_PAGE_FAIL), ERROR, null);
        }

        if (favoriteManager.has(userRef, docRef)) {
            return getStatus(l10n.getTranslationPlain("favorites.user.addCurrentPage.alreadyExists"), WARNING, null);
        }

        String saveComment = l10n.getTranslationPlain(FAVORITES_USER_UPDATE_PROFILE);
        try {
            favoriteManager.addAll(userRef, Collections.singletonList(docRef), saveComment);
            return getStatus(l10n.getTranslationPlain("favorites.user.addCurrentPage.success"), DONE, null);
        } catch (FavoritesException e) {
            return getStatus(l10n.getTranslationPlain(FAVORITES_USER_ADD_CURRENT_PAGE_FAIL), ERROR, e);
        }
    }

    /**
     * @param userRef the user for which to remove the favorite
     * @param docRef the entity to unfavorite (for now, only documents)
     * @return a map with a success and a status members, for JSON serialization
     */
    public Map<String, String> remove(DocumentReference userRef, DocumentReference docRef) throws FavoritesException
    {
        String saveComment = l10n.getTranslationPlain(FAVORITES_USER_UPDATE_PROFILE);
        if (!favoriteManager.removeAll(userRef, Collections.singletonList(docRef), saveComment)) {
            return getStatus(l10n.getTranslationPlain("favorites.user.removeCurrentPage.doesntExist"), WARNING, null);
        }
        return getStatus(l10n.getTranslationPlain("favorites.user.removeCurrentPage.success"), DONE, null);
    }

    private static Map<String, String> getStatus(String message, String status, Exception cause)
    {
        Map<String, String> s = new LinkedHashMap<>(cause == null ? 2 : 3);
        s.put("message", message);
        s.put("status", status);
        if (cause != null) {
            s.put("reason", cause.getMessage());
        }
        return s;
    }
}
