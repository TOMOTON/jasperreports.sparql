package eionet.jasperreports.cds;

import com.jaspersoft.ireport.designer.data.ReportQueryDialog;
import com.jaspersoft.ireport.designer.FieldsProvider;
import com.jaspersoft.ireport.designer.FieldsProviderEditor;
import com.jaspersoft.ireport.designer.IReportConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRPropertiesMap;

import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;
import org.eclipse.rdf4j.query.QueryLanguage;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;


public class SPARQLQueryFieldsProvider implements FieldsProvider {

    private static final HashMap<String, String> XSD_TYPES = new HashMap<String, String>();
    static {
        XSD_TYPES.put("string", "java.lang.String");
        XSD_TYPES.put("int", "java.lang.Integer");
        XSD_TYPES.put("boolean", "java.lang.Boolean");
        XSD_TYPES.put("float", "java.lang.Float");
        XSD_TYPES.put("double", "java.lang.Double");
        XSD_TYPES.put("integer", "java.math.BigInteger");
        XSD_TYPES.put("decimal", "java.math.BigDecimal");
        XSD_TYPES.put("date", "java.sql.Date");
        XSD_TYPES.put("dateTime", "java.sql.Time");
    }

    @Override
    public boolean supportsGetFieldsOperation() {
        return true;
    }

    // Note: The properties in the report are not copied to the reportDataset object.
    @Override
    public JRField[] getFields(IReportConnection con, JRDataset reportDataset, Map parameters )
            throws JRException, UnsupportedOperationException {

        String queryString = reportDataset.getQuery().getText();
        String limitedQuery = getLimitedQuery(queryString);
//      JRPropertiesMap m = reportDataset.getPropertiesMap();
//      String endpointUrl = m.getProperty("endpoint");
        String endpointUrl = null;
        if (parameters.containsKey("endpoint")) {
            endpointUrl = parameters.get("endpoint").toString();
        }
        ArrayList fields = new ArrayList();

        SPARQLDataSource ds = new SPARQLDataSource(endpointUrl, limitedQuery);
        ds.next();
        BindingSet currentBinding = ds.getCurrentRow();

        for (String fieldName : currentBinding.getBindingNames()) {
            Value value = currentBinding.getValue(fieldName);
            JRDesignField field = new JRDesignField();
            field.setName(fieldName);
            String javaType = getJavaType(value);
            field.setValueClassName(javaType);
            fields.add(field);
        }
        return (JRField[]) fields.toArray(new JRField[fields.size()]);

    }

    private String getJavaType(Value value) {

        if (value == null) {
            return "java.lang.String"; // Variable was unbound in result
        }
        if (value instanceof Literal) {
            Literal lValue = (Literal) value;
            URI dataType = lValue.getDatatype();
            if (dataType == null) {
                return "java.lang.String";
            }
            String namespace = dataType.getNamespace();
            String dataTypeFrag = dataType.getLocalName();
            if ("http://www.w3.org/2001/XMLSchema#".equals(namespace)) {
                String javaType = XSD_TYPES.get(dataTypeFrag);
                if (javaType != null) {
                    return javaType;
                }
            }
        }
        return "java.lang.String"; // Put an elephant in Cairo
    }

    /**
     * Alternative implementation.
     */
    public JRField[] getFieldsAlt(IReportConnection con, JRDataset reportDataset, Map parameters )
            throws JRException, UnsupportedOperationException {
        ArrayList fields = new ArrayList();

        try {
            String queryString = reportDataset.getQuery().getText();
            ParsedTupleQuery pq = QueryParserUtil.parseTupleQuery(QueryLanguage.SPARQL, queryString, null);
            TupleExpr te = pq.getTupleExpr();
            Set<String> vars = te.getBindingNames();
            for (String var : vars) {
                JRDesignField field = new JRDesignField();
                field.setName(var);
                field.setValueClassName("java.lang.String");
                fields.add(field);
            }
            return (JRField[]) fields.toArray(new JRField[fields.size()]);
        } catch (Exception e) {
            throw new JRException("Something went wrong");
        } 

    }

    /**
     * Create a query that has a limit of 1 row to return.
     * Could also remove the OFFSET clause.
     *
     * @param queryString - The query string to change.
     */
    String getLimitedQuery(String queryString) {
        int lastBracket = queryString.lastIndexOf('}');
        String queryStringLim = queryString;

        String endPart = queryString.substring(lastBracket);
        String unLimited = endPart.replaceAll("[Ll][Ii][Mm][Ii][Tt]\\s*[0-9]+", "");
        return queryString.substring(0, lastBracket) + unLimited + " LIMIT 1";
    }

    @Override
    public boolean supportsAutomaticQueryExecution() {
        return false;
    }

    @Override
    public boolean hasEditorComponent() {
        return false;
    }

    @Override
    public boolean hasQueryDesigner() {
        return false;
    }

    @Override
    public String designQuery(IReportConnection con, String query, ReportQueryDialog reportQueryDialog )
            throws JRException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldsProviderEditor getEditorComponent(ReportQueryDialog reportQueryDialog ) {
        return null;
    }

}

