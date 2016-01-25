package org.draff;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.draff.objectdb.ObjectDb;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by dave on 1/23/16.
 */
public class EmbeddedCommunityFollowersCounts {
  private ObjectDb db;
  private String outputFile;
  private static int MAX_COMMUNITIES = 100;

  public EmbeddedCommunityFollowersCounts(ObjectDb db, String outputFile) {
    this.db = db;
    this.outputFile = outputFile;
  }

  public void saveFollowersCounts() {
    List<EmbeddedCommunity> communities = db.find(EmbeddedCommunity.class, MAX_COMMUNITIES);
    List<String> lowerScreenNames = new ArrayList<>();
    communities.forEach(community -> {
      lowerScreenNames.add(community.embeddedScreenName.toLowerCase());
      lowerScreenNames.add(community.parentScreenName.toLowerCase());
    });

    Map<String, UserDetail> userDetails = userDetailsMap(lowerScreenNames);

    try {
      Writer writer = new FileWriter(outputFile);
      CSVPrinter printer = new CSVPrinter(writer, CSVFormat.EXCEL);

      for (EmbeddedCommunity community : communities) {
        printer.printRecord(
            community.embeddedScreenName,
            userDetails.get(community.embeddedScreenName.toLowerCase()).followersCount
        );
        printer.printRecord(
            community.parentScreenName,
            userDetails.get(community.parentScreenName.toLowerCase()).followersCount
        );
      }
      printer.close();
      writer.close();
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  private Map<String, UserDetail> userDetailsMap(List<String> screenNames) {
    List<String> lowerScreenNames = screenNames.stream().map(String::toLowerCase)
        .collect(Collectors.toList());
    Map<String, UserDetail> usersMap = new HashMap<>();
    Map<String, Object> constraint = new HashMap<>();
    for (String lowerScreenName : lowerScreenNames) {
      constraint.put("screenNameLower", lowerScreenName);
      UserDetail detail = db.findOne(UserDetail.class, constraint);
      usersMap.put(lowerScreenName, detail);
    }
    return usersMap;
  }
}
