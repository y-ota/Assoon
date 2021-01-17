package jp.assoon.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AssoonUtilsTest {

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  @Before
  public void setUp() throws Exception {}

  @Test
  public void testMaxValueTopic() {
    double[] values = new double[] {0.0, 0.5, 10.5, 3.0};
    assertThat(2, equalTo(AssoonUtils.maxValueTopic(values)));
  }

  @Test
  public void testMaxValueTopicWithDuplicateValues() {
    double[] values = new double[] {0.0, 10.5, 10.5, 3.0};
    assertThat(null, equalTo(AssoonUtils.maxValueTopic(values)));
  }

  @Test
  public void testDeleteOldDirectories() throws IOException {
    tempFolder.newFolder("202001011213000");
    tempFolder.newFolder("202001021213000");
    tempFolder.newFolder("202001031213000");
    tempFolder.newFolder("202001041213000");
    tempFolder.newFolder("202001051213000");

    AssoonUtils.deleteOldDataDirectories(tempFolder.getRoot().toPath(), 3);

    assertThat(
        false, equalTo(tempFolder.getRoot().toPath().resolve("202001011213000").toFile().exists()));
    assertThat(
        false, equalTo(tempFolder.getRoot().toPath().resolve("202001021213000").toFile().exists()));
    assertThat(
        true, equalTo(tempFolder.getRoot().toPath().resolve("202001031213000").toFile().exists()));
    assertThat(
        true, equalTo(tempFolder.getRoot().toPath().resolve("202001041213000").toFile().exists()));
    assertThat(
        true, equalTo(tempFolder.getRoot().toPath().resolve("202001051213000").toFile().exists()));
  }
}
