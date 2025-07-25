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
package org.xwiki.contrib.favorites;

import java.util.Collection;

import org.xwiki.component.annotation.Role;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.user.UserReference;

/**
 * Favorites Manager.
 * @since 1.4.0
 * @version $Id$
 */
@Role
public interface FavoriteManager
{
    /**
     * Add the given document as a favorite for the given user.
     * NOTE: passing an entity reference to something else than a document is currently undefined behavior, but
     * might be possible and defined in the future.
     *
     * @param userRef The user for which to add the favorite
     * @param docRef The document to favorite
     * @throws FavoritesException if something wrong happens
     * @return whether the favorite has been added (typically false if the favorite was already there)
     */
    boolean add(EntityReference userRef, EntityReference docRef) throws FavoritesException;

    /**
     * Add the given document as a favorite for the given user.
     * NOTE: passing an entity reference to something else than a document is currently undefined behavior, but
     * might be possible and defined in the future.
     *
     * @param userRef The user for which to add the favorite
     * @param docRef The document to favorite
     * @throws FavoritesException if something wrong happens
     * @return whether the favorite has been added (typically false if the favorite was already there)
     */
    boolean add(UserReference userRef, EntityReference docRef) throws FavoritesException;

    /**
     * Add the given document as a favorite for the given user.
     * NOTE: passing entity references to something else than documents is currently undefined behavior, but
     * might be possible and defined in the future.
     *
     * @param userRef The user for which to add the favorite
     * @param docRefs The documents to favorite
     * @throws FavoritesException if something wrong happens
     * @return whether at least one favorite has been added (favorites are typically not added if already there)
     */
    boolean addAll(EntityReference userRef, Collection<EntityReference> docRefs)
        throws FavoritesException;

    /**
     * Add the given document as a favorite for the given user.
     * NOTE: passing entity references to something else than documents is currently undefined behavior, but
     * might be possible and defined in the future.
     *
     * @param userRef The user for which to add the favorite
     * @param docRefs The documents to favorite
     * @throws FavoritesException if something wrong happens
     * @return whether at least one favorite has been added (favorites are typically not added if already there)
     */
    boolean addAll(UserReference userRef, Collection<EntityReference> docRefs)
        throws FavoritesException;
    /**
     * Add the given document as a favorite for the given user.
     * NOTE: passing an entity reference to something else than a document is currently undefined behavior, but
     * might be possible and defined in the future.
     *
     * @param userRef The user for which to add the favorite
     * @param docRefs The documents to favorite
     * @param saveComment The version comment to use when saving the user profile
     * @throws FavoritesException if something wrong happens
     * @return whether at least one favorite has been added (favorites are typically not added if already there)
     */
    boolean addAll(EntityReference userRef, Collection<EntityReference> docRefs, String saveComment)
        throws FavoritesException;

    /**
     * Add the given document as a favorite for the given user.
     * NOTE: passing entity references to something else than documents is currently undefined behavior, but
     * might be possible and defined in the future.
     *
     * @param userRef The user for which to add the favorite
     * @param docRefs The documents to favorite
     * @param saveComment The version comment to use when saving the user profile
     * @throws FavoritesException if something wrong happens
     * @return whether at least one favorite has been added (favorites are typically not added if already there)
     */
    boolean addAll(UserReference userRef, Collection<EntityReference> docRefs, String saveComment)
        throws FavoritesException;

    /**
     * Remove the given document as a favorite for the given user.
     * NOTE: passing an entity reference to something else than a document is currently undefined behavior, but
     * might be possible and defined in the future.
     *
     * @param userRef The user for which to add the favorite
     * @param docRef The document to favorite
     * @throws FavoritesException if something wrong happens
     * @return whether the favorite was removed (typically false if the favorite wasn't there in the first place)
     */
    boolean remove(EntityReference userRef, EntityReference docRef) throws FavoritesException;

    /**
     * Remove the given document as a favorite for the given user.
     * NOTE: passing an entity reference to something else than a document is currently undefined behavior, but
     * might be possible and defined in the future.
     *
     * @param userRef The user for which to add the favorite
     * @param docRef The document to favorite
     * @throws FavoritesException if something wrong happens
     * @return whether the favorite was removed (typically false if the favorite wasn't there in the first place)
     */
    boolean remove(UserReference userRef, EntityReference docRef) throws FavoritesException;

    /**
     * Remove the given documents as favorites for the given user.
     * NOTE: passing entity references to something else than documents is currently undefined behavior, but
     * might be possible and defined in the future.
     *
     * @param userRef The user for which to add the favorite
     * @param docRefs The document to favorite
     * @throws FavoritesException if something wrong happens
     * @return whether any favorite was removed (typically false if none of the favorites were there in the first place)
     */
    boolean removeAll(EntityReference userRef, Collection<EntityReference> docRefs) throws FavoritesException;

    /**
     * Remove the given document as a favorite for the given user.
     * NOTE: passing an entity reference to something else than a document is currently undefined behavior, but
     * might be possible and defined in the future.
     *
     * @param userRef The user for which to add the favorite
     * @param docRefs The document to favorite
     * @param saveComment The version comment to use when saving the user profile
     * @throws FavoritesException if something wrong happens
     * @return whether any favorite was removed (typically false if none of the favorites were there in the first place)
     */
    boolean removeAll(EntityReference userRef, Collection<EntityReference> docRefs, String saveComment)
        throws FavoritesException;

    /**
     * Remove the given document as a favorite for the given user.
     * NOTE: passing an entity reference to something else than a document is currently undefined behavior, but
     * might be possible and defined in the future.
     *
     * @param userRef The user for which to add the favorite
     * @param docRefs The document to favorite
     * @param saveComment The version comment to use when saving the user profile
     * @throws FavoritesException if something wrong happens
     * @return whether any favorite was removed (typically false if none of the favorites were there in the first place)
     */
    boolean removeAll(UserReference userRef, Collection<EntityReference> docRefs, String saveComment)
        throws FavoritesException;

    /**
     * @return Whether the given document is a favorite of the given user.
     * NOTE: passing an entity reference to something else than a document is currently undefined behavior, but
     * might be possible and defined in the future.
     *
     * @param userRef The user for which to add the favorite
     * @param docRef The document to favorite
     * @throws FavoritesException if something wrong happens
     */
    boolean has(EntityReference userRef, EntityReference docRef) throws FavoritesException;

    /**
     * @return Whether the given document is a favorite of the given user.
     * NOTE: passing an entity reference to something else than a document is currently undefined behavior, but
     * might be possible and defined in the future.
     *
     * @param userRef The user for which to add the favorite
     * @param docRef The document to favorite
     * @throws FavoritesException if something wrong happens
     */
    boolean has(UserReference userRef, EntityReference docRef) throws FavoritesException;

}
