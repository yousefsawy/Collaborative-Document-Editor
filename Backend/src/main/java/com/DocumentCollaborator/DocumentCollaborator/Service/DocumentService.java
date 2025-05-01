package com.DocumentCollaborator.DocumentCollaborator.Service;
import java.util.concurrent.ConcurrentHashMap;

import CRDT.Node;
import com.DocumentCollaborator.DocumentCollaborator.DTO.DocumentCreateResponse;
import com.DocumentCollaborator.DocumentCollaborator.Model.Document;
import org.springframework.stereotype.Service;

@Service
public class DocumentService {
    ConcurrentHashMap<String, Document> systemDocuments=new ConcurrentHashMap<String,Document>();
    ConcurrentHashMap<String, String> EditorIds = new ConcurrentHashMap<String, String>();
    ConcurrentHashMap<String, String> ViewerIds = new ConcurrentHashMap<String, String>();



    public DocumentCreateResponse createDocument(String title, String username, String content){

        Document doc=new Document(title, username, content);
        setDocument(doc);
        return new DocumentCreateResponse(doc.getDocumentId(), doc.getDocumentName(), doc.getEditorId(), doc.getViewerId(), doc.getOwnerId());
    }

    public DocumentCreateResponse createDocument(String title, String username){
        Document doc=new Document(title, username);
        setDocument(doc);
        return new DocumentCreateResponse(doc.getDocumentId(), doc.getDocumentName(), doc.getEditorId(), doc.getViewerId(), doc.getOwnerId());
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

    public Node[] getDocumentNodes(String documentId){
        return getDocument(documentId).getDocumentNodes();
    }

    private void setDocument(Document doc)
    {
        if(systemDocuments.putIfAbsent(doc.getDocumentId(), doc) == null) {
            EditorIds.put(doc.getEditorId(), doc.getDocumentId());
            ViewerIds.put(doc.getViewerId(), doc.getDocumentId());
        };
    }
}
