package com.luguosong.ssiach11ex4.repositories;


import com.luguosong.ssiach11ex4.model.Document;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class DocumentRepository {

    private Map<String, Document> documents =
            Map.of("abc123", new Document("natalie"),
                    "qwe123", new Document("natalie"),
                    "asd555", new Document("emma"));


    public Document findDocument(String code) {
        return documents.get(code);
    }
}
