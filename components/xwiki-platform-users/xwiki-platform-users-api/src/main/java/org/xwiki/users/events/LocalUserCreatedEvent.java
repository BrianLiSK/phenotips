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
package org.xwiki.users.events;

import org.xwiki.users.User;

/**
 * Notifies that a new local user profile has been created.
 *
 * @version $Id$
 * @since 1.0RC1
 */
public class LocalUserCreatedEvent extends AbstractUserEvent
{
    /**
     * Simple constructor passing all the required information.
     *
     * @param user the new user
     * @param author the user performing this action
     */
    public LocalUserCreatedEvent(User user, User author)
    {
        super("localUserCreated", user, author);
    }

    /** Default constructor, to be used for declaring the events a listener wants to observe. */
    public LocalUserCreatedEvent()
    {
        this(null, null);
    }
}
