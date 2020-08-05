package com.example.demolucene;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
//@SpringBootTest
class DemoLuceneApplicationTests {

    private static List<Emplyee> list;

    static {
        list = Lists.newArrayList();
        list.add(new Emplyee(1L, "张三", "江苏南京"));
        list.add(new Emplyee(2L, "李四", "江苏扬州"));
        list.add(new Emplyee(3L, "王五", "广东广州"));
    }

    @Test
    void storeTest() {
        List<Document> docList = new ArrayList<>();
        for (Emplyee emplyee : list) {
            Document doc = new Document();
            doc.add(new StringField("id", emplyee.getId().toString(), Field.Store.YES));
            doc.add(new StringField("name", emplyee.getName(), Field.Store.YES));
            doc.add(new TextField("address", emplyee.getAddress(), Field.Store.YES));
            docList.add(doc);
        }
        try (
                Directory dir = FSDirectory.open(Paths.get("E:\\dir"));
                IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()))) {
            writer.addDocuments(docList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void readTest() {
        QueryParser queryParser = new QueryParser("address", new StandardAnalyzer());
        try {
            Directory dir = FSDirectory.open(Paths.get("E:\\dir"));
            IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
            TopDocs docs = searcher.search(queryParser.parse("江苏"), 10);
            log.info("docs num:{}", docs.totalHits);
            for (ScoreDoc scoreDoc : docs.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                log.info(doc.get("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Emplyee {
    private Long id;
    private String name;
    private String address;
}
