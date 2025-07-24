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
package org.xwiki.contrib.favorites.internal.filter.output;

import java.io.IOException;
import java.util.Locale;

import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.contrib.favorites.FavoritesException;
import org.xwiki.contrib.favorites.FavoriteManager;
import org.xwiki.filter.FilterEventParameters;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.output.AbstractBeanOutputFilterStream;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.user.UserReference;

/**
 * Favorite Instance Output Filter Stream.
 * @since 1.4.0
 * @version $Id$
 */
@Component
@Named(FavoriteInstanceOutputFilterStreamFactory.ROLEHINT)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class FavoriteInstanceOutputFilterStream extends AbstractBeanOutputFilterStream<FavoriteInstanceOutputProperties>
    implements FavoriteInstanceOutputFilter
{
    private EntityReference currentReference;

    @Inject
    private FavoriteManager favoriteManager;

    @Override
    public void close() throws IOException
    {
        // Nothing to close
    }

    @Override
    public void onUserFavorite(EntityReference userRef, FilterEventParameters parameters) throws FilterException
    {
        if (currentReference != null) {
            try {
                favoriteManager.add(userRef, currentReference);
            } catch (FavoritesException e) {
                throw new FilterException(e);
            }
        }
    }

    @Override
    public void onUserFavorite(UserReference userRef, FilterEventParameters parameters) throws FilterException
    {
        if (currentReference != null) {
            try {
                favoriteManager.add(userRef, currentReference);
            } catch (FavoritesException e) {
                throw new FilterException(e);
            }
        }
    }

    @Override
    public void beginWikiDocument(String name, FilterEventParameters parameters)
    {
        currentReference = new EntityReference(name, EntityType.DOCUMENT, currentReference);
    }

    @Override
    public void endWikiDocument(String name, FilterEventParameters parameters) throws FilterException
    {
        backToParent(name);
    }

    @Override
    public void beginWikiDocumentLocale(Locale locale, FilterEventParameters parameters)
    {
        // ignore
    }

    @Override
    public void endWikiDocumentLocale(Locale locale, FilterEventParameters parameters)
    {
        // ignore
    }

    @Override
    public void beginWikiDocumentRevision(String revision, FilterEventParameters parameters)
    {
        // ignore
    }

    @Override
    public void endWikiDocumentRevision(String revision, FilterEventParameters parameters)
    {
        // ignore
    }

    @Override
    public void beginWiki(String name, FilterEventParameters parameters)
    {
        currentReference = new WikiReference(name);
    }

    @Override
    public void endWiki(String name, FilterEventParameters parameters)
    {
        currentReference = null;
    }

    @Override
    public void beginWikiSpace(String name, FilterEventParameters parameters)
    {
        currentReference = new EntityReference(name, EntityType.SPACE, currentReference);
    }

    @Override
    public void endWikiSpace(String name, FilterEventParameters parameters)
    {
        backToParent(name);
    }

    private void backToParent(String name)
    {
        if (currentReference != null && (name == null || currentReference.getName().equals(name))) {
            currentReference = currentReference.getParent();
        }
    }
}
