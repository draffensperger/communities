package org.draff;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.draff.model.EmbeddedCommunity;
import org.draff.objectdb.ObjectDb;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by dave on 1/21/16.
 */
public class EmbeddedCommunityLoader {
  private ObjectDb db;
  private String csvFile;

  public EmbeddedCommunityLoader(ObjectDb db, String csvFile) {
    this.db = db;
    this.csvFile = csvFile;
  }

  public void loadEmbeddedCommunities() {
    try {
      CSVParser parser = new CSVParser(new FileReader(csvFile), CSVFormat.EXCEL.withHeader());
      List<EmbeddedCommunity> communities = parser.getRecords().stream().map(csvRecord ->
        new EmbeddedCommunity(csvRecord.get("Embedded Screen Name"),
            csvRecord.get("Parent Screen Name"))).collect(Collectors.toList());
      db.saveAll(communities);

    } catch(IOException e) {
      e.printStackTrace();
    }
  }
}
