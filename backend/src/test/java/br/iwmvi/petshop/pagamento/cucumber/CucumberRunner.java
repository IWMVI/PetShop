package br.iwmvi.petshop.pagamento.cucumber;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("br/iwmvi/petshop/pagamento/cucumber")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "br.iwmvi.petshop.pagamento.cucumber")
public class CucumberRunner {
}
