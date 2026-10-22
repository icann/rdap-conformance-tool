package org.icann.rdapconformance.validator.workflow.rdap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

import java.util.List;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.general.ResponseValidation1Dot2_1_2024;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.general.ResponseValidationGtldProfile3;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation1Dot3_2024;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RDAPValidatorGtldProfileV3Test {

    private QueryContext queryContext;
    private RDAPValidator validator;

    @BeforeMethod
    public void setUp() {
        queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
        doReturn(true).when(config).check();
        doReturn(true).when(config).isGtldRegistry();
        doReturn(true).when(config).isGtldRegistrar();
        doReturn(false).when(config).isNetworkEnabled();
        doReturn(java.net.URI.create("https://example.com/domain/test.example")).when(config).getUri();
        doReturn(config).when(queryContext).getConfig();

        doReturn("{}").when(queryContext).getRdapResponseData();

        validator = new RDAPValidator(queryContext);
    }

    @Test
    public void gtldProfileV3_excludesOnly61000And62000() {
        List<ProfileValidation> v2024 = validator.get2024ProfileValidations(null);
        List<ProfileValidation> v3 = validator.getGtldProfileV3Validations(null);

        // Excluded tests are absent
        assertThat(v3)
                .noneMatch(v -> v instanceof TigValidation1Dot3_2024)       // -61000
                .noneMatch(v -> v instanceof ResponseValidation1Dot2_1_2024); // -62000

        // Baseline 2024 contains them
        assertThat(v2024)
                .anyMatch(v -> v instanceof TigValidation1Dot3_2024)
                .anyMatch(v -> v instanceof ResponseValidation1Dot2_1_2024);

        // v3-specific additions are present
        assertThat(v3)
                .anyMatch(v -> v instanceof ResponseValidationGtldProfile3); // -66000

        // Exactly the same scope minus the two exclusions, plus the v3 additions
        assertThat(v3).hasSize(v2024.size() - 2 + 1);

        List<Class<?>> v3Classes = v3.stream()
                .filter(v -> !(v instanceof ResponseValidationGtldProfile3))
                .<Class<?>>map(Object::getClass)
                .toList();

        List<Class<?>> expectedClasses = v2024.stream()
                .filter(v -> !(v instanceof TigValidation1Dot3_2024)
                        && !(v instanceof ResponseValidation1Dot2_1_2024))
                .<Class<?>>map(Object::getClass)
                .toList();

        assertThat(v3Classes).containsExactlyElementsOf(expectedClasses);
    }
}