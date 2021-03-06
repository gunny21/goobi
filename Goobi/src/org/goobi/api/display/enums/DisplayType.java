package org.goobi.api.display.enums;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. - http://www.goobi.org - http://launchpad.net/goobi-production - http://gdz.sub.uni-goettingen.de -
 * http://www.intranda.com - http://digiverso.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
public enum DisplayType {

    input("0", "input", "InputPlugin"),
    select("1", "select", "MultiSelectPlugin"),
    select1("2", "select1", "SingleSelectPlugin"),
    textarea("3", "textarea", "TextAreaPlugin"),
    readonly("4", "readonly", "ReadOnlyPlugin"),
    gnd("5", "gnd", "GndInputPlugin"),
    person("6", "person", "PersonPlugin"),
    geonames("7", "geonames", "GeonamesPlugin")
    ;

    private String id;
    private String title;
    private String pluginName;

    private DisplayType(String myId, String myTitle, String pluginName) {
        this.id = myId;
        this.title = myTitle;
        this.pluginName = pluginName;
    }

    public String getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getPluginName() {
        return pluginName;
    }

    public static DisplayType getByTitle(String inTitle) {
        if (inTitle != null) {
            for (DisplayType type : DisplayType.values()) {
                if (type.getTitle().equals(inTitle)) {
                    return type;
                }
            }
        }
        return input; // textarea is default
    }
}
