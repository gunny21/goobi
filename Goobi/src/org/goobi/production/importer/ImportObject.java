package org.goobi.production.importer;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
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
import java.util.ArrayList;
import java.util.List;

import org.goobi.beans.Masterpieceproperty;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Templateproperty;
import org.goobi.production.enums.ImportReturnValue;

public class ImportObject {

    // must end with ".xml" in current implementation
    private String processTitle = "";
    private Integer batchId;

    private String metsFilename = "";

    private String importFileName = "";

    // error handling
    private ImportReturnValue importReturnValue = ImportReturnValue.ExportFinished;
    private String errorMessage = "";

    // additional information
    private List<Processproperty> processProperties = new ArrayList<Processproperty>();
    private List<Masterpieceproperty> workProperties = new ArrayList<Masterpieceproperty>();
    private List<Templateproperty> templateProperties = new ArrayList<Templateproperty>();

    public ImportObject() {

    }

    public String getProcessTitle() {
        return this.processTitle;
    }

    public void setProcessTitle(String processTitle) {
        this.processTitle = processTitle;
    }

    public String getMetsFilename() {
        return this.metsFilename;
    }

    public void setMetsFilename(String metsFilename) {
        this.metsFilename = metsFilename;
    }

    public ImportReturnValue getImportReturnValue() {
        return this.importReturnValue;
    }

    public void setImportReturnValue(ImportReturnValue importReturnValue) {
        this.importReturnValue = importReturnValue;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<Processproperty> getProcessProperties() {
        return this.processProperties;
    }

    public void setProcessProperties(List<Processproperty> processProperties) {
        this.processProperties = processProperties;
    }

    public List<Masterpieceproperty> getWorkProperties() {
        return this.workProperties;
    }

    public void setWorkProperties(List<Masterpieceproperty> workProperties) {
        this.workProperties = workProperties;
    }

    public List<Templateproperty> getTemplateProperties() {
        return this.templateProperties;
    }

    public void setTemplateProperties(List<Templateproperty> templateProperties) {
        this.templateProperties = templateProperties;
    }

    public Integer getBatchId() {
        return batchId;
    }

    public void setBatchId(Integer batchId) {
        this.batchId = batchId;
    }

    public String getImportFileName() {
        return importFileName;
    }

    public void setImportFileName(String importFileName) {
        this.importFileName = importFileName;
    }

}
