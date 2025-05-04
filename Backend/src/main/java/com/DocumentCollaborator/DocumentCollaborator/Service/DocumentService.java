package com.DocumentCollaborator.DocumentCollaborator.Service;
import java.util.concurrent.ConcurrentHashMap;

import CRDT.Node;
import CRDT.Operation;
import com.DocumentCollaborator.DocumentCollaborator.DTO.DocumentCreateResponse;
import com.DocumentCollaborator.DocumentCollaborator.Model.Document;
import com.DocumentCollaborator.DocumentCollaborator.Model.User;
import org.springframework.stereotype.Service;

@Service
public class DocumentService {
    ConcurrentHashMap<String, Document> systemDocuments=new ConcurrentHashMap<String,Document>();
    ConcurrentHashMap<String, String> EditorIds = new ConcurrentHashMap<String, String>();
    ConcurrentHashMap<String, String> ViewerIds = new ConcurrentHashMap<String, String>();



    public DocumentCreateResponse createDocument(String title, String username, String content){
        Document doc=new Document(title, username, content);
        setDocument(doc);
        return new DocumentCreateResponse(doc.getDocumentId(), doc.getDocumentName(), doc.getEditorId(), doc.getViewerId());
    }

    public DocumentCreateResponse getDocumentIds(String documentId){
        Document doc = getDocument(documentId);
        if (doc==null){ return null;}
        return new DocumentCreateResponse(doc.getDocumentId(), doc.getDocumentName(), doc.getEditorId(), doc.getViewerId());
    }

    public DocumentCreateResponse createDocument(String title, String username){
        Document doc=new Document(title, username);
        setDocument(doc);
        return new DocumentCreateResponse(doc.getDocumentId(), doc.getDocumentName(), doc.getEditorId(), doc.getViewerId());
    }


    public Document getDocument(String documentId){
        if (systemDocuments.containsKey(documentId)) {
            System.out.println("Id");
            return systemDocuments.get(documentId);
        }
        if (EditorIds.containsKey(documentId)) {
            System.out.println("EditorId");
            return systemDocuments.get(EditorIds.get(documentId));
        }
        if (ViewerIds.containsKey(documentId)) {
            System.out.println("ViewerId");
            return systemDocuments.get(ViewerIds.get(documentId));
        }
        return null;
    }

    public Node[] getDocumentNodes(String documentId)
    {
        if (getDocument(documentId) != null) {
            return getDocument(documentId).getDocumentNodes();
        }
        return null;
    }

    public void handleDocumentOperation(String documentId, Operation operation){
        if (getDocument(documentId) != null) {
            getDocument(documentId).handleOperation(operation);
        }
    }
    private void setDocument(Document doc)
    {
        if(systemDocuments.putIfAbsent(doc.getDocumentId(), doc) == null) {
            EditorIds.put(doc.getEditorId(), doc.getDocumentId());
            ViewerIds.put(doc.getViewerId(), doc.getDocumentId());
        };
    }

    public User[] getDocumentUsers(String documentId) {
        Document document = getDocument(documentId);
        if (document != null && !document.getUsers().isEmpty()) {
            return document.getUsers().values().toArray(new User[0]);
        }
        return new User[0];
    }
    public boolean isEdtior(String documentId) {
        return EditorIds.containsKey(documentId) || systemDocuments.containsKey(documentId);
    }
}
