/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.telve.jcr.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletResponse;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.tika.io.IOUtils;
import org.modeshape.jcr.api.JcrTools;
import org.primefaces.event.FileUploadEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JCR Arayüzü için control sınıfı.
 * 
 * Kullanımı için Home beanler üzerinden init edilmesi gerekir.
 * 
 * @author Hakan Uygun 
 */
public class JcrController {
    
    private static final Logger LOG = LoggerFactory.getLogger(JcrController.class);
    
    private String contextRoot;
    private String sourceDomain;
    private String sourceCaption;
    private Long sourceId;

    
    private String selectedPath;
    private String newFolderName;
    
    private List<Node> folders;
    private List<FileInfo> files;
    
    public JcrController(String contextRoot, String sourceDomain, String sourceCaption, Long sourceId) {
        this.contextRoot = contextRoot;
        this.sourceDomain = sourceDomain;
        this.sourceCaption = sourceCaption;
        this.sourceId = sourceId;
        this.selectedPath = contextRoot;
    }

    public String getContextRoot() {
        return contextRoot;
    }

    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }

    public String getSourceDomain() {
        return sourceDomain;
    }

    public void setSourceDomain(String sourceDomain) {
        this.sourceDomain = sourceDomain;
    }

    public String getSourceCaption() {
        return sourceCaption;
    }

    public void setSourceCaption(String sourceCaption) {
        this.sourceCaption = sourceCaption;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }
    
    public List<Node> getFolders() throws RepositoryException{
        if( folders == null ){
            
            populateFolders();
        }
        return folders;
    }

    private void populateFolders() throws RepositoryException {
        folders = new ArrayList<>();
        
        Session session = getSession();
        JcrTools jcrTools = new JcrTools();
        Node node = jcrTools.findOrCreateNode(session, getContextRoot(), "nt:folder");

        popuplateFolderNodes( node );
    }
    
    private void popuplateFolderNodes(Node node) throws RepositoryException {
        folders.add(node);
        NodeIterator it = node.getNodes();
        while (it.hasNext()) {
            Node n = it.nextNode();
            if (n.isNodeType("nt:folder")) {
                popuplateFolderNodes(n);
            }
        }
    }
    
    /**
     * Geriye JCR Session instance'ı döndürür.
     * @return 
     */
    private Session getSession() {
        return BeanProvider.getContextualReference(Session.class);
    }

    
    public List<FileInfo> getFiles() throws RepositoryException{
        if( files == null ){
            populateFiles();
        }
        
        return files;
    }
    
    /**
     * Buraya nt:file tipinde node gelir ve fileInfo döner.
     *
     * @param node
     */
    private FileInfo buildFileInfo(Node node) throws RepositoryException {
        FileInfo fm = new FileInfo();

        fm.setId(node.getIdentifier());
        fm.setName(node.getName());
        fm.setPath(node.getPath());

        fm.setCreateBy(node.getProperty("jcr:createdBy").getString());
        fm.setCreateDate(node.getProperty("jcr:created").getDate().getTime());

        /*
        if (node.isNodeType("tlv:ref")) {
            fm.setSourceDomain(node.getProperty("tlv:sourceDomain").getString());
            fm.setSourceCaption(node.getProperty("tlv:sourceCaption").getString());
            fm.setSourceId(node.getProperty("tlv:sourceId").getLong());
            
        }
        
        if (node.isNodeType("tlv:tag")) {
                        
            if( node.hasProperty("tlv:info") ){
                fm.setInfo(node.getProperty("tlv:info").getString());
            }
            
            if( node.hasProperty("tlv:category") ){
                fm.setCategory(node.getProperty("tlv:category").getString());
            }
            //TODO: Taglar için sanırım array almak lazım
            //fm.setTags(node.getProperty("tlv:tags").getString());
        }
        */
        Node cn = node.getNode("jcr:content");

        fm.setMimeType(cn.getProperty("jcr:mimeType").getString());

        return fm;
    }

    private void populateFiles() throws RepositoryException {
        populateFiles(getSelectedPath());
    }
    
    private void populateFiles( String folder ) throws RepositoryException {
        files = new ArrayList<>();
        
        Session session = getSession();
        JcrTools jcrTools = new JcrTools();
        Node node = jcrTools.findOrCreateNode(session, folder, "nt:folder");

        popuplateFileNodes( node );
    }

    private void popuplateFileNodes(Node node) throws RepositoryException {
        
        NodeIterator it = node.getNodes();
        while (it.hasNext()) {
            Node n = it.nextNode();
            if (n.isNodeType("nt:file")) {
                files.add(buildFileInfo(n));
            }
        }
    }
    
    public void newFolder() throws RepositoryException {
        newFolder(newFolderName);
    }
    
    public void newFolder( String folderName ) throws RepositoryException {
        LOG.info("Folder Name: {}", folderName);

        
        Session session = getSession();

        JcrTools jcrTools = new JcrTools();
        Node folder = jcrTools.findOrCreateNode(session, getSelectedPath() + "/" + folderName, "nt:folder");

        LOG.info("Folder Node: {}", folder);

        session.save();

        //FacesMessage msg = new FacesMessage("Success! ", folderName + " is created.");
        //FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public void handleFileUpload(FileUploadEvent event) throws RepositoryException {
        LOG.info("Uploaded File : {}", event.getFile().getFileName());

        String fileNamePath = event.getFile().getFileName();
        String fileName = fileNamePath.substring(fileNamePath.lastIndexOf(File.separatorChar) + 1);

        try {

            String folderName = getSelectedPath();
            String path = folderName + "/" + fileName;

            LOG.info("Folder Name : {}", path);
            copyFile(path, event.getFile().getInputstream());

        } catch (IOException e) {
            LOG.error("IO Error", e);
        }
        //FacesMessage msg = new FacesMessage("Success! ", fileName + " is uploaded.");
        //FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    
    public void copyFile(String fileName, InputStream in) {
        
        Session session = getSession();

        try {
            JcrTools jcrTools = new JcrTools();
            Node n = jcrTools.uploadFile(session, fileName, in);

            //n.addMixin("tlv:ref");
            //n.addMixin("tlv:tag");
            //n.setProperty("tlv:refName", getContext().getReferanceName());
            //n.setProperty("tlv:refView", getContext().getReferanceView());
            //n.setProperty("tlv:refID", getContext().getReferanceId());

            session.save();

            //View Modele de ekleyelim.
            files.add(buildFileInfo(n));
            

            LOG.info("Dosya JCR'e kondu : {}", fileName);
            
        } catch (RepositoryException ex) {
            LOG.error("Reporsitory Exception", ex);
        } catch (IOException ex) {
            LOG.error("IO Exception", ex);
        }

    }

    public String getSelectedPath() {
        return selectedPath;
    }

    public void setSelectedPath(String selectedPath) {
        this.selectedPath = selectedPath;
    }
    

    public void downloadFile(String id) throws RepositoryException {
        Session session = getSession();

        Node node = session.getNodeByIdentifier(id);

        Node content = node.getNode("jcr:content");

        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();

        response.setContentType(content.getProperty("jcr:mimeType").getString());

        response.setHeader("Content-disposition", "attachment;filename=" + node.getName());
        response.setContentLength((int) content.getProperty("jcr:data").getBinary().getSize());

        try {

            try (OutputStream out = response.getOutputStream()) {
                IOUtils.copy(content.getProperty("jcr:data").getBinary().getStream(), out);
                
                out.flush();
            }

            FacesContext.getCurrentInstance().responseComplete();
        } catch (IOException ex) {
            LOG.error("Error while downloading the file", ex);
            //facesMessages.add("Error while downloading the file: " + fileName + type );
        }
    }
    
    /**
     * Verilen mimeType için tanımlı icon path'ini döndürür.
     *
     * Bulamazsa generic bir icon path döndürür
     *
     * @param mimeType
     * @return
     */
    public String getIcon(String mimeType) {
        switch( mimeType ){
            case "application/pdf" : return "fa-file-pdf-o";
            case "image/png" : return "fa-file-image-o";
            case "image/jpg" : return "fa-file-image-o";
            case "image/jpeg" : return "fa-file-image-o";
            case "text/plain" : return "fa-file-text-o";
            case "application/msword" :
            case "application/vnd.oasis.opendocument.text" : return "fa-file-word-o";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
            case "application/vnd.ms-excel" :
            case "application/vnd.oasis.opendocument.spreadsheet": return "fa-file-excel-o";

            default: return "fa-file-o";
        }
    }
    
    
    public void selectNode() throws RepositoryException {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String id = params.get("nodeId");
        Session session = getSession();

        Node node = session.getNodeByIdentifier(id);
        
        setSelectedPath(node.getPath());
        
        populateFiles();
    }

    public String getNewFolderName() {
        return newFolderName;
    }

    public void setNewFolderName(String newFolderName) {
        this.newFolderName = newFolderName;
    }
    
    public void deleteFile(String id) throws RepositoryException {
        Session session = getSession();

        Node node = session.getNodeByIdentifier(id);

        node.remove();

        session.save();

        //View Modelden de silelim
        for (FileInfo m : files) {
            if (id.equals(m.getId())) {
                files.remove(m);
                break;
            }
        }
    }
}