package eionet.jasperreports.cds;

import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;
import org.eclipse.rdf4j.query.QueryLanguage;

import org.junit.Test;
import org.junit.Assert;

public class QueryFieldsTest {

    @Test
    public void simpleQuery() {
        SPARQLQueryFieldsProvider qfp = new SPARQLQueryFieldsProvider();
        String input = "SELECT * WHERE { ?a a ?b }";
        String expected = "SELECT * WHERE { ?a a ?b } LIMIT 1";
        Assert.assertEquals(expected, qfp.getLimitedQuery(input));
    }

    @Test
    public void simpleLimitedQuery() {
        SPARQLQueryFieldsProvider qfp = new SPARQLQueryFieldsProvider();
        String input = "SELECT * WHERE { ?a a ?b } group by ?b limit 20000";
        String expected = "SELECT * WHERE { ?a a ?b } group by ?b  LIMIT 1";
        Assert.assertEquals(expected, qfp.getLimitedQuery(input));
    }

    @Test
    public void simpleOffsetQuery() {
        SPARQLQueryFieldsProvider qfp = new SPARQLQueryFieldsProvider();
        String input = "SELECT * WHERE { ?a a ?b } limit 20000 OFFSET 10000";
        String expected = "SELECT * WHERE { ?a a ?b }  OFFSET 10000 LIMIT 1";
        Assert.assertEquals(expected, qfp.getLimitedQuery(input));
    }

}

