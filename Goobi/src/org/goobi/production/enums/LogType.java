package org.goobi.production.enums;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://www.intranda.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 * 
 */

import lombok.Getter;

public enum LogType {

    ERROR("error"),
    WARN("warn"),
    DEBUG("debug"),
    USER("user");

    @Getter
    private String title;

    private LogType(String title) {
        this.title = title;
    }

    public static LogType getByTitle(String title) {
        for (LogType t : LogType.values()) {
            if (t.getTitle().equals(title)) {
                return t;
            }
        }
        return null;
    }
}