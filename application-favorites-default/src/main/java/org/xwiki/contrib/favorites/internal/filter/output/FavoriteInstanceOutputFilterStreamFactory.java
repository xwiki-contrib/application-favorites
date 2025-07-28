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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.favorites.FavoriteManager;
import org.xwiki.filter.AbstractFilterStreamFactory;
import org.xwiki.filter.descriptor.AbstractFilterStreamDescriptor;
import org.xwiki.filter.descriptor.FilterStreamDescriptor;
import org.xwiki.filter.instance.output.OutputInstanceFilterStreamFactory;
import org.xwiki.filter.output.OutputFilterStream;
import org.xwiki.filter.type.FilterStreamType;

/**
 * Favorite Instance Output Filter Stream Factory.
 * @since 1.4.0
 * @version $Id$
 */
@Component
@Singleton
@Named(FavoriteInstanceOutputFilterStreamFactory.ID)
public class FavoriteInstanceOutputFilterStreamFactory extends AbstractFilterStreamFactory implements
    OutputInstanceFilterStreamFactory
{
    static final String ID = "favorite";

    private static final Collection<Class<?>> FILTER_INTERFACES =
        Collections.singletonList(FavoriteInstanceOutputFilter.class);

    private static final FilterStreamDescriptor FILTER_DESCRIPTOR = new AbstractFilterStreamDescriptor(
        "XWiki favorites instance output stream",
        "Specialized version of the XWiki instance output stream for favorites."
    ) { };

    @Inject
    private FavoriteManager favoriteManager;

    /**
     * Constructor.
     */
    public FavoriteInstanceOutputFilterStreamFactory()
    {
        super(new FilterStreamType(FilterStreamType.XWIKI_INSTANCE.getType(),
            FilterStreamType.XWIKI_INSTANCE.getDataFormat() + "+" + ID));

        setDescriptor(FILTER_DESCRIPTOR);
    }

    @Override
    public Collection<Class<?>> getFilterInterfaces()
    {
        return FILTER_INTERFACES;
    }

    @Override
    public OutputFilterStream createOutputFilterStream(Map<String, Object> properties)
    {
        return new FavoriteInstanceOutputFilterStream(favoriteManager);
    }
}
