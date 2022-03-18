package fr.insee.kraftwerk.core.rawdata;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class QuestionnaireDataTest {

    private QuestionnaireData questionnaireData;

    @BeforeEach
    public void initQuestionnaireData() {
        questionnaireData = new QuestionnaireData();
    }

    @Test
    public void putAndGetFromGroupInstance() {

        GroupInstance root = questionnaireData.getAnswers();

        // Put a value at the root
        root.putValue("FOO", "foo");
        // depth 1
        root.getSubGroup("DEPTH1").getInstance(0).putValue("FOO1", "foo1");
        // depth 2
        root.getSubGroup("DEPTH1").getInstance(0)
                .getSubGroup("DEPTH2").getInstance(0).putValue("FOO2", "foo2");

        //
        assertEquals("foo", root.getValue("FOO"));
        assertEquals("foo1", root.getSubGroup("DEPTH1").getInstance(0)
                .getValue("FOO1"));
        assertEquals("foo2", root.getSubGroup("DEPTH1").getInstance(0)
                .getSubGroup("DEPTH2").getInstance(0)
                .getValue("FOO2"));
        //
        assertNull(root.getValue("UNKNOWN"));
        assertNull(root.getSubGroup("DEPTH1").getInstance(0)
                .getValue("UNKNOWN1"));
        assertNull(root.getSubGroup("DEPTH1").getInstance(0)
                .getSubGroup("DEPTH2").getInstance(0)
                .getValue("UNKNOWN2"));
    }

    @Test
    public void putAndGetFromGroupData() {

        GroupInstance root = questionnaireData.getAnswers();

        // Put values from group data objects
        GroupData groupDepth1 = root.getSubGroup("DEPTH1");
        GroupData groupDepth2 = root.getSubGroup("DEPTH1").getInstance(0)
                .getSubGroup("DEPTH2");
        groupDepth1.putValue("foo1", "FOO1", 0);
        groupDepth2.putValue("foo2", "FOO2", 0);

        //
        assertEquals("foo1", root.getSubGroup("DEPTH1").getValue("FOO1", 0));
        assertEquals("foo2", root.getSubGroup("DEPTH1").getInstance(0)
                .getSubGroup("DEPTH2").getValue("FOO2", 0));
        //
        assertNull(root.getSubGroup("DEPTH1").getValue("UNKNOWN1", 0));
        assertNull(root.getSubGroup("DEPTH1").getInstance(0)
                .getSubGroup("DEPTH2").getValue("UNKNOWN2", 0));
    }

    @Test
    public void putAndGetFromQuestionnaireData() {
        // Put a value from questionnaire data
        // at root
        questionnaireData.putValue("foo", "FOO");
        // depth 1
        questionnaireData.putValue("foo1", "FOO1", Pair.of("DEPTH1", 0));
        // depth 2
        questionnaireData.putValue("foo2", "FOO2", Pair.of("DEPTH1", 0),
                Pair.of("DEPTH2", 0));

        //
        assertEquals("foo", questionnaireData.getValue("FOO"));
        assertEquals("foo1", questionnaireData.getValue("FOO1", Pair.of("DEPTH1", 0)));
        assertEquals("foo2", questionnaireData.getValue("FOO2", Pair.of("DEPTH1", 0),
                Pair.of("DEPTH2", 0)));
        //
        assertNull(questionnaireData.getValue("UNKNOWN"));
        assertNull(questionnaireData.getValue("UNKNOWN1", Pair.of("DEPTH1", 0)));
        assertNull(questionnaireData.getValue("UNKNOWN2", Pair.of("DEPTH1", 0),
                Pair.of("DEPTH2", 0)));
    }

}
