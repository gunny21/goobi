package de.sub.goobi.forms;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.goobi.production.enums.ImportFormat;
import org.goobi.production.enums.ImportReturnValue;
import org.goobi.production.enums.ImportType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.export.ExportDocket;
import org.goobi.production.flow.helper.JobCreation;
import org.goobi.production.importer.DocstructElement;
import org.goobi.production.importer.GoobiHotfolder;
import org.goobi.production.importer.ImportObject;
import org.goobi.production.importer.Record;
import org.goobi.production.plugin.ImportPluginLoader;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IImportPlugin;
import org.goobi.production.properties.ImportProperty;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import ugh.dl.Prefs;

import org.goobi.beans.Process;
import org.goobi.beans.Step;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;

@ManagedBean(name = "MassImportForm")
@SessionScoped
public class MassImportForm {
    private static final Logger logger = Logger.getLogger(MassImportForm.class);
    private Process template;
    private List<Process> processes;
    private List<String> digitalCollections;
    private List<String> possibleDigitalCollections;
    // private List<String> recordList = new ArrayList<String>();
    private List<String> ids = new ArrayList<String>();
    private ImportFormat format = null;
    private String idList = "";
    private String records = "";
    // private List<String> usablePlugins = new ArrayList<String>();
    private List<String> usablePluginsForRecords = new ArrayList<String>();
    private List<String> usablePluginsForIDs = new ArrayList<String>();
    private List<String> usablePluginsForFiles = new ArrayList<String>();
    private List<String> usablePluginsForFolder = new ArrayList<String>();
    private final ImportPluginLoader ipl = new ImportPluginLoader();
    private String currentPlugin = "";
    private IImportPlugin plugin;

    private Path importFile = null;
    private final Helper help = new Helper();
    // private ImportConfiguration ic = null;

    private Part uploadedFile = null;

    private List<Process> processList;

    // progress bar
    private Integer progress = 0;
    private Integer currentProcessNo = 0;
    private Integer totalProcessNo = 0;
    
    public MassImportForm() {

        // usablePlugins = ipl.getTitles();
        this.usablePluginsForRecords = this.ipl.getPluginsForType(ImportType.Record);
        this.usablePluginsForIDs = this.ipl.getPluginsForType(ImportType.ID);
        this.usablePluginsForFiles = this.ipl.getPluginsForType(ImportType.FILE);
        this.usablePluginsForFolder = this.ipl.getPluginsForType(ImportType.FOLDER);

    }

    public String Prepare() {
        if (this.template.getContainsUnreachableSteps()) {
            if (this.template.getSchritteList().size() == 0) {
                Helper.setFehlerMeldung("noStepsInWorkflow");
            }
            for (Step s : this.template.getSchritteList()) {
                if (s.getBenutzergruppenSize() == 0 && s.getBenutzerSize() == 0) {
                    Helper.setFehlerMeldung(Helper.getTranslation("noUserInStep", s.getTitel()));
                }
            }
            return "";
        }
        initializePossibleDigitalCollections();
        return "process_import_1";
    }

    /**
     * generate a list with all possible collections for given project
     * 
     * 
     */

    private void initializePossibleDigitalCollections() {
        this.possibleDigitalCollections = new ArrayList<String>();
        ArrayList<String> defaultCollections = new ArrayList<String>();
        String filename = this.help.getGoobiConfigDirectory() + "goobi_digitalCollections.xml";
        if (!Files.exists(Paths.get(filename))) {
            Helper.setFehlerMeldung("File not found: ", filename);
            return;
        }
        this.digitalCollections = new ArrayList<String>();
        try {
            /* Datei einlesen und Root ermitteln */
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(filename);
            Element root = doc.getRootElement();
            /* alle Projekte durchlaufen */
            List<Element> projekte = root.getChildren();
            for (Iterator<Element> iter = projekte.iterator(); iter.hasNext();) {
                Element projekt = iter.next();

                // collect default collections
                if (projekt.getName().equals("default")) {
                    List<Element> myCols = projekt.getChildren("DigitalCollection");
                    for (Iterator<Element> it2 = myCols.iterator(); it2.hasNext();) {
                        Element col = it2.next();

                        if (col.getAttribute("default") != null && col.getAttributeValue("default").equalsIgnoreCase("true")) {
                            digitalCollections.add(col.getText());
                        }

                        defaultCollections.add(col.getText());
                    }
                } else {
                    // run through the projects
                    List<Element> projektnamen = projekt.getChildren("name");
                    for (Iterator<Element> iterator = projektnamen.iterator(); iterator.hasNext();) {
                        Element projektname = iterator.next();
                        // all all collections to list
                        if (projektname.getText().equalsIgnoreCase(this.template.getProjekt().getTitel())) {
                            List<Element> myCols = projekt.getChildren("DigitalCollection");
                            for (Iterator<Element> it2 = myCols.iterator(); it2.hasNext();) {
                                Element col = it2.next();

                                if (col.getAttribute("default") != null && col.getAttributeValue("default").equalsIgnoreCase("true")) {
                                    digitalCollections.add(col.getText());
                                }

                                this.possibleDigitalCollections.add(col.getText());
                            }
                        }
                    }
                }
            }
        } catch (JDOMException e1) {
            logger.error("error while parsing digital collections", e1);
            Helper.setFehlerMeldung("Error while parsing digital collections", e1);
        } catch (IOException e1) {
            logger.error("error while parsing digital collections", e1);
            Helper.setFehlerMeldung("Error while parsing digital collections", e1);
        }

        if (this.possibleDigitalCollections.size() == 0) {
            this.possibleDigitalCollections = defaultCollections;
        }
    }

    private List<String> allFilenames = new ArrayList<String>();
    private List<String> selectedFilenames = new ArrayList<String>();

    public List<String> getAllFilenames() {

        return this.allFilenames;
    }

    public void setAllFilenames(List<String> allFilenames) {
        this.allFilenames = allFilenames;
    }

    public List<String> getSelectedFilenames() {
        return this.selectedFilenames;
    }

    public void setSelectedFilenames(List<String> selectedFilenames) {
        this.selectedFilenames = selectedFilenames;
    }

    public String convertData() {
        this.processList = new ArrayList<Process>();
        if (StringUtils.isEmpty(currentPlugin)) {
            Helper.setFehlerMeldung("missingPlugin");
            return "";
        }
        if (testForData()) {


            List<ImportObject> answer = new ArrayList<ImportObject>();
            Integer batchId = null;

            // found list with ids
            Prefs prefs = this.template.getRegelsatz().getPreferences();
            String tempfolder = ConfigurationHelper.getInstance().getTemporaryFolder();
            this.plugin.setImportFolder(tempfolder);
            this.plugin.setPrefs(prefs);
            
            if (StringUtils.isNotEmpty(this.idList)) {
                // IImportPlugin plugin = (IImportPlugin)
                // PluginLoader.getPlugin(PluginType.Import,
                // this.currentPlugin);
                List<String> ids = this.plugin.splitIds(this.idList);
                List<Record> recordList = new ArrayList<Record>();
                for (String id : ids) {
                    Record r = new Record();
                    r.setData(id);
                    r.setId(id);
                    r.setCollections(this.digitalCollections);
                    recordList.add(r);
                }
                totalProcessNo = recordList.size() * 2;
                answer = this.plugin.generateFiles(recordList);
            } else if (this.importFile != null) {
                // uploaded file
                // IImportPlugin plugin = (IImportPlugin)
                // PluginLoader.getPlugin(PluginType.Import,
                // this.currentPlugin);
                this.plugin.setFile(this.importFile.toFile());
                List<Record> recordList = this.plugin.generateRecordsFromFile();
                for (Record r : recordList) {
                    r.setCollections(this.digitalCollections);
                }
                totalProcessNo = recordList.size() * 2;
                answer = this.plugin.generateFiles(recordList);
            } else if (StringUtils.isNotEmpty(this.records)) {
                // found list with records
                // IImportPlugin plugin = (IImportPlugin)
                // PluginLoader.getPlugin(PluginType.Import,
                // this.currentPlugin);
                List<Record> recordList = this.plugin.splitRecords(this.records);
                for (Record r : recordList) {
                    r.setCollections(this.digitalCollections);
                }
                totalProcessNo = recordList.size() * 2;
                answer = this.plugin.generateFiles(recordList);
            } else if (this.selectedFilenames.size() > 0) {
                List<Record> recordList = this.plugin.generateRecordsFromFilenames(this.selectedFilenames);
                for (Record r : recordList) {
                    r.setCollections(this.digitalCollections);
                }
                totalProcessNo = recordList.size() * 2;
                answer = this.plugin.generateFiles(recordList);

            }

            if (answer.size() > 1) {
                //				Session session = Helper.getHibernateSession();

                batchId = 1;
                try {
                    batchId += ProcessManager.getMaxBatchNumber();
                } catch (Exception e1) {
                }

            }
            for (ImportObject io : answer) {
               
                if (batchId != null) {
                    io.setBatchId(batchId);
                }
                if (io.getImportReturnValue().equals(ImportReturnValue.ExportFinished)) {
                    Process p = JobCreation.generateProcess(io, this.template);
                    // int returnValue =
                    // HotfolderJob.generateProcess(io.getProcessTitle(),
                    // this.template, new File(tempfolder), null, "error", b);
                    if (p == null) {
                        if (io.getImportFileName() != null && !io.getImportFileName().isEmpty() && selectedFilenames != null
                                && !selectedFilenames.isEmpty()) {
                            if (selectedFilenames.contains(io.getImportFileName())) {
                                selectedFilenames.remove(io.getImportFileName());
                            }
                        }
                        Helper.setFehlerMeldung("import failed for " + io.getProcessTitle() + ", process generation failed");

                    } else {
                        Helper.setMeldung(ImportReturnValue.ExportFinished.getValue() + " for " + io.getProcessTitle());
                        this.processList.add(p);
                    }
                } else {
                    String[] parameter = { io.getProcessTitle(), io.getErrorMessage() };
                    Helper.setFehlerMeldung(Helper.getTranslation("importFailedError", parameter));
                    // Helper.setFehlerMeldung("import failed for: " + io.getProcessTitle() + " Error message is: " + io.getErrorMessage());
                    if (io.getImportFileName() != null && !io.getImportFileName().isEmpty() && selectedFilenames != null
                            && !selectedFilenames.isEmpty()) {
                        if (selectedFilenames.contains(io.getImportFileName())) {
                            selectedFilenames.remove(io.getImportFileName());
                        }
                    }
                }
                currentProcessNo = currentProcessNo + 1;
            }
            if (answer.size() != this.processList.size()) {
                // some error on process generation, don't go to next page
                return "";
            }
        }
        // missing data
        else {
            Helper.setFehlerMeldung("missingData");
            return "";
        }
        currentProcessNo = 0;
        this.idList = null;
        if (this.importFile != null) {
            try {
                Files.delete(this.importFile);
            } catch (IOException e) {
                logger.error(e);
            }
            this.importFile = null;
        }
        if (selectedFilenames != null && !selectedFilenames.isEmpty()) {
            this.plugin.deleteFiles(this.selectedFilenames);
        }
        this.records = "";
        return "process_import_3";
    }

    /**
     * File upload with binary copying.
     */
    public void uploadFile() {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            if (this.uploadedFile == null) {
                Helper.setFehlerMeldung("noFileSelected");
                return;
            }

            String basename = getFileName(this.uploadedFile);
            if (basename.startsWith(".")) {
                basename = basename.substring(1);
            }
            if (basename.contains("/")) {
                basename = basename.substring(basename.lastIndexOf("/") + 1);
            }
            if (basename.contains("\\")) {
                basename = basename.substring(basename.lastIndexOf("\\") + 1);
            }

            String filename = ConfigurationHelper.getInstance().getTemporaryFolder() + basename;

            inputStream = this.uploadedFile.getInputStream();
            outputStream = new FileOutputStream(filename);

            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }

            this.importFile = Paths.get(filename);
            Helper.setMeldung(Helper.getTranslation("uploadSuccessful", basename));
            // Helper.setMeldung("File '" + basename + "' successfully uploaded, press 'Save' now...");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            Helper.setFehlerMeldung("uploadFailed");
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }

        }

    }

    private String getFileName(final Part part) {
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

    public Part getUploadedFile() {
        return this.uploadedFile;
    }

    public void setUploadedFile(Part uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    /**
     * tests input fields for correct data
     * 
     * @return true if data is valid or false otherwise
     */

    private boolean testForData() {
        // if (format == null) {
        // return false;
        // }
        if (StringUtils.isEmpty(this.idList) && StringUtils.isEmpty(this.records) && (this.importFile == null) && this.selectedFilenames.size() == 0) {
            return false;
        }
        return true;
    }

    /**
     * 
     * @return list with all import formats
     */
    public List<String> getFormats() {
        List<String> l = new ArrayList<String>();
        for (ImportFormat input : ImportFormat.values()) {
            l.add(input.getTitle());
        }
        return l;
    }

    @Deprecated
    public String getHotfolderPathForPlugin(int pluginId) {
        for (GoobiHotfolder hotfolder : GoobiHotfolder.getInstances()) {
            if (hotfolder.getTemplate() == pluginId) {
                return hotfolder.getFolderAsString();
            }
        }

        return null;
    }

    /**
     * 
     * @return current format
     */
    @Deprecated
    public String getCurrentFormat() {
        if (this.format != null) {
            return this.format.getTitle();
        } else {
            return "";
        }
    }

    /**
     * 
     * @param formatTitle current format
     */
    @Deprecated
    public void setCurrentFormat(String formatTitle) {
        this.format = ImportFormat.getTypeFromTitle(formatTitle);
    }

    /**
     * @param idList the idList to set
     */
    public void setIdList(String idList) {
        this.idList = idList;
    }

    /**
     * @return the idList
     */
    public String getIdList() {
        return this.idList;
    }

    /**
     * @param records the records to set
     */
    public void setRecords(String records) {
        this.records = records;
    }

    /**
     * @return the records
     */
    public String getRecords() {
        return this.records;
    }

    /**
     * @param process the process to set
     */
    public void setProcess(List<Process> processes) {
        this.processes = processes;
    }

    /**
     * @return the process
     */
    public List<Process> getProcess() {
        return this.processes;
    }

    /**
     * @param template the template to set
     */
    public void setTemplate(Process template) {
        // this.ic = new ImportConfiguration(template);
        this.template = template;

    }

    /**
     * @return the template
     */
    public Process getTemplate() {
        return this.template;
    }

    /**
     * @param digitalCollections the digitalCollections to set
     */
    public void setDigitalCollections(List<String> digitalCollections) {
        this.digitalCollections = digitalCollections;
    }

    /**
     * @return the digitalCollections
     */
    public List<String> getDigitalCollections() {
        return this.digitalCollections;
    }

    /**
     * @param possibleDigitalCollection the possibleDigitalCollection to set
     */
    public void setPossibleDigitalCollection(List<String> possibleDigitalCollection) {
        this.possibleDigitalCollections = possibleDigitalCollection;
    }

    /**
     * @return the possibleDigitalCollection
     */
    public List<String> getPossibleDigitalCollection() {
        return this.possibleDigitalCollections;
    }

    @Deprecated
    public void setProcesses(List<Process> processes) {
        this.processes = processes;
    }

    /**
     * @param ids the ids to set
     */
    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    /**
     * @return the ids
     */
    public List<String> getIds() {
        return this.ids;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(String format) {
        this.format = ImportFormat.getTypeFromTitle(format);
    }

    /**
     * @return the format
     */
    public String getFormat() {
        if (this.format == null) {
            return "";
        }
        return this.format.getTitle();
    }

    /**
     * @param currentPlugin the currentPlugin to set
     */
    public void setCurrentPlugin(String currentPlugin) {
        this.currentPlugin = currentPlugin;
        if (currentPlugin != null && currentPlugin.length() > 0) {
            this.plugin = (IImportPlugin) PluginLoader.getPluginByTitle(PluginType.Import, this.currentPlugin);
            if (this.plugin.getImportTypes().contains(ImportType.FOLDER)) {
                this.allFilenames = this.plugin.getAllFilenames();
            }
            plugin.setPrefs(template.getRegelsatz().getPreferences());
            plugin.setForm(this);
        }
    }

    /**
     * @return the currentPlugin
     */
    public String getCurrentPlugin() {
        return this.currentPlugin;
    }

    public IImportPlugin getPlugin() {
        return plugin;
    }

    /**
     * @param usablePluginsForRecords the usablePluginsForRecords to set
     */
    public void setUsablePluginsForRecords(List<String> usablePluginsForRecords) {
        this.usablePluginsForRecords = usablePluginsForRecords;
    }

    /**
     * @return the usablePluginsForRecords
     */
    public List<String> getUsablePluginsForRecords() {
        return this.usablePluginsForRecords;
    }

    /**
     * @param usablePluginsForIDs the usablePluginsForIDs to set
     */
    public void setUsablePluginsForIDs(List<String> usablePluginsForIDs) {
        this.usablePluginsForIDs = usablePluginsForIDs;
    }

    /**
     * @return the usablePluginsForIDs
     */
    public List<String> getUsablePluginsForIDs() {
        return this.usablePluginsForIDs;
    }

    /**
     * @param usablePluginsForFiles the usablePluginsForFiles to set
     */
    public void setUsablePluginsForFiles(List<String> usablePluginsForFiles) {
        this.usablePluginsForFiles = usablePluginsForFiles;
    }

    /**
     * @return the usablePluginsForFiles
     */
    public List<String> getUsablePluginsForFiles() {
        return this.usablePluginsForFiles;
    }

    public boolean getHasNextPage() {
        java.lang.reflect.Method method;
        try {
            method = this.plugin.getClass().getMethod("getCurrentDocStructs");
            Object o = method.invoke(this.plugin);
            @SuppressWarnings("unchecked")
            List<? extends DocstructElement> list = (List<? extends DocstructElement>) o;
            if (this.plugin != null && list != null) {
                return true;
            }
        } catch (Exception e) {
        }
        try {
            method = this.plugin.getClass().getMethod("getProperties");
            Object o = method.invoke(this.plugin);
            @SuppressWarnings("unchecked")
            List<ImportProperty> list = (List<ImportProperty>) o;
            if (this.plugin != null && list.size() > 0) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public String nextPage() {
        if (!testForData()) {
            Helper.setFehlerMeldung("missingData");
            return "";
        }
        java.lang.reflect.Method method;
        try {
            method = this.plugin.getClass().getMethod("getCurrentDocStructs");
            Object o = method.invoke(this.plugin);
            @SuppressWarnings("unchecked")
            List<? extends DocstructElement> list = (List<? extends DocstructElement>) o;
            if (this.plugin != null && list != null) {
                return "process_import_2_mass";
            }
        } catch (Exception e) {
        }
        return "process_import_2";
    }

    public List<ImportProperty> getProperties() {

        if (this.plugin != null) {
            return this.plugin.getProperties();
        }
        return new ArrayList<ImportProperty>();
    }

    public List<Process> getProcessList() {
        return this.processList;
    }

    public void setProcessList(List<Process> processList) {
        this.processList = processList;
    }

    public List<String> getUsablePluginsForFolder() {
        return this.usablePluginsForFolder;
    }

    public void setUsablePluginsForFolder(List<String> usablePluginsForFolder) {
        this.usablePluginsForFolder = usablePluginsForFolder;
    }

    public String downloadDocket() {
        if (logger.isDebugEnabled()) {
            logger.debug("generate docket for process list");
        }
        String rootpath = ConfigurationHelper.getInstance().getXsltFolder();
        Path xsltfile = Paths.get(rootpath, "docket_multipage.xsl");
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        if (!facesContext.getResponseComplete()) {
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            String fileName = "batch_docket" + ".pdf";
            ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
            String contentType = servletContext.getMimeType(fileName);
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");

            // write docket to servlet output stream
            try {
                ServletOutputStream out = response.getOutputStream();
                ExportDocket ern = new ExportDocket();
                ern.startExport(this.processList, out, xsltfile.toString());
                out.flush();
            } catch (IOException e) {
                logger.error("IOException while exporting run note", e);
            }

            facesContext.responseComplete();
        }
        return "";
    }

    public List<? extends DocstructElement> getDocstructs() {
        java.lang.reflect.Method method;
        try {
            method = this.plugin.getClass().getMethod("getCurrentDocStructs");
            Object o = method.invoke(this.plugin);
            @SuppressWarnings("unchecked")
            List<? extends DocstructElement> list = (List<? extends DocstructElement>) o;
            if (this.plugin != null && list != null) {
                return list;
            }
        } catch (Exception e) {
        }
        return new ArrayList<DocstructElement>();
    }

    public String getPagePath() {
        java.lang.reflect.Method method;
        try {
            method = this.plugin.getClass().getMethod("getPagePath");
            Object o = method.invoke(this.plugin);
           if (o != null) {
               String path = (String) o;
               return path;
           }
            
        } catch (Exception e) {
        }
        return null;
    }
    
    
    
    public int getDocstructssize() {
        return getDocstructs().size();
    }

    @Deprecated
    public String getInclude() {
        return "plugins/" + plugin.getTitle() + ".jsp";
    }

    public Integer getProgress() {
        if (progress == null) {
            progress = 0;
        } else if (totalProcessNo == 0) {
            progress = 100;
        } else {
            progress = (int) (currentProcessNo * 100 / totalProcessNo);

            if (progress > 100)
                progress = 100;
        }

        return progress;
    }

    public void setCurrentProcess(Integer number) {
        currentProcessNo = number;
    }

    public void setTotalProcessNo(Integer totalProcessNo) {
        this.totalProcessNo = totalProcessNo;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public void onComplete() {
        progress = null;
    }

    public boolean isShowProgressBar() {
        if (progress == null || progress == 100 || progress == 0) {
            return false;
        }
        return true;
    }

    public void addProcessToProgressBar() {
        currentProcessNo = currentProcessNo + 1;
    }
}
