package com.example.demolucene;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
import org.apache.lucene.queryparser.xml.builders.RangeQueryBuilder;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.format.datetime.joda.LocalDateParser;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
class DemoLuceneApplicationTests {

    private static List<Emplyee> list;
    public static final String PATH_INDEX = "E:\\dir";
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");

    static {
        list = Lists.newArrayList();
        list.add(new Emplyee(1L, "张三",24,LocalDate.of(1996,10,6), "江苏南京"));
        list.add(new Emplyee(2L, "李四",30,LocalDate.of(1990,8,22), "江苏扬州"));
        list.add(new Emplyee(3L, "王五",20,LocalDate.of(2000,11,6), "广东广州"));
    }

    @Test
    void storeTest() {
        List<Document> docList = new ArrayList<>();
        for (Emplyee emplyee : list) {
            Document doc = new Document();
            doc.add(new StringField("id", emplyee.getId().toString(), Field.Store.YES));
            doc.add(new StringField("name", emplyee.getName(), Field.Store.YES));
            doc.add(new IntPoint("age", emplyee.getAge()));
            doc.add(new StringField("birth", dtf.format(emplyee.getBirth()), Field.Store.YES));
            doc.add(new TextField("address", emplyee.getAddress(), Field.Store.YES));
            docList.add(doc);
        }
        try (
                Directory dir = FSDirectory.open(Paths.get(PATH_INDEX));
                IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(new IKAnalyzer()))) {
            writer.addDocuments(docList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void readTest() {
        QueryParser queryParser = new QueryParser("address", new IKAnalyzer());
//        Query query = IntPoint.newRangeQuery("age", 25, 36);
        try {
            Query query1 = queryParser.parse("birth:[19961006 TO 20200808]");
            Query query2 = queryParser.parse("name:张三");
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            builder.add(query1, BooleanClause.Occur.MUST);
            builder.add(query2, BooleanClause.Occur.MUST);

            Directory dir = FSDirectory.open(Paths.get(PATH_INDEX));
            IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
//            TopDocs docs = searcher.search(queryParser.parse("江苏南京"), 10);
//            TopDocs docs = searcher.search(query2, 10);
            TopDocs docs = searcher.search(builder.build(), 10);
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
    private Integer age;
    private LocalDate birth;
    private String address;
}
