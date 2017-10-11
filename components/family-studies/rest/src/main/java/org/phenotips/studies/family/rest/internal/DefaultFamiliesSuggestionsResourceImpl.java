/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/
 */
package org.phenotips.studies.family.rest.internal;

import org.phenotips.studies.family.internal.export.PhenotipsFamilyExport;
import org.phenotips.studies.family.rest.FamiliesSuggestionsResource;

import org.xwiki.component.annotation.Component;
import org.xwiki.rest.XWikiResource;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;

/**
 * Default implementation for {@link FamiliesSuggestionsResource} using XWiki's support for REST resources.
 *
 * @version $Id$
 * @since 1.4M3
 */
@Component
@Named("org.phenotips.data.rest.internal.DefaultFamiliesSuggestionsResourceImpl")
@Singleton
public class DefaultFamiliesSuggestionsResourceImpl extends XWikiResource implements FamiliesSuggestionsResource
{
    @Inject
    private PhenotipsFamilyExport familyExport;

    @Override
    public String suggest(String input, @DefaultValue("10") int maxResults,
        @DefaultValue("view") String requiredPermission, @DefaultValue("id") String orderField,
        @DefaultValue("asc") String order)
    {
        return this.familyExport.searchFamilies(input, maxResults, requiredPermission, orderField, order);
    }

}
