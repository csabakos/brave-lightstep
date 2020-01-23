package brave.propagation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class LightStepPropagationTests {
  private static final long TRACE_ID = 1234;
  private static final long SPAN_ID = 5678;
  private static final String ENCODED_TRACE_ID = "00000000000004d2";
  private static final String ENCODED_SPAN_ID = "000000000000162e";
  private static final LightStepPropagation<String> LIGHT_STEP_PROPAGATION =
      new LightStepPropagation<>(Propagation.KeyFactory.STRING);
  private static final TraceContext.Injector<Map<String, String>> INJECTOR =
      LIGHT_STEP_PROPAGATION.injector(
          new Propagation.Setter<Map<String, String>, String>() {
            @Override
            public void put(Map<String, String> stringStringMap, String key, String value) {
              stringStringMap.put(key, value);
            }
          });
  private static final TraceContext.Extractor<Map<String, String>> EXTRACTOR =
      LIGHT_STEP_PROPAGATION.extractor(
          new Propagation.Getter<Map<String, String>, String>() {
            @Override
            public String get(Map<String, String> stringStringMap, String key) {
              return stringStringMap.get(key);
            }
          });
  private static final TraceContext NOT_SAMPLED_CONTEXT =
      TraceContext.newBuilder().traceId(TRACE_ID).spanId(SPAN_ID).build();
  private static final TraceContext SAMPLED_CONTEXT =
      NOT_SAMPLED_CONTEXT.toBuilder().sampled(true).build();

  @Test
  public void testKeys() {
    assertThat(LIGHT_STEP_PROPAGATION.keys())
        .containsExactlyInAnyOrder("ot-tracer-traceid", "ot-tracer-spanid", "ot-tracer-sampled");
  }

  @Test
  public void testInjectorNotSampled() {
    final Map<String, String> carrier = new HashMap<>();
    INJECTOR.inject(NOT_SAMPLED_CONTEXT, carrier);
    assertThat(carrier)
        .doesNotContainKey("ot-tracer-sampled")
        .contains(
            entry("ot-tracer-traceid", ENCODED_TRACE_ID),
            entry("ot-tracer-spanid", ENCODED_SPAN_ID));
  }

  @Test
  public void testInjectorSampled() {
    final Map<String, String> carrier = new HashMap<>();
    INJECTOR.inject(SAMPLED_CONTEXT, carrier);
    assertThat(carrier)
        .contains(
            entry("ot-tracer-traceid", ENCODED_TRACE_ID),
            entry("ot-tracer-spanid", ENCODED_SPAN_ID),
            entry("ot-tracer-sampled", "true"));
  }

  @Test
  public void testExtractorEmpty() {
    assertThat(EXTRACTOR.extract(Collections.<String, String>emptyMap()))
        .isEqualTo(TraceContextOrSamplingFlags.EMPTY);
  }

  @DataProvider
  public static String[] dataProviderFalsy() {
    return new String[] {"", "false", "foobar"};
  }

  @Test
  @UseDataProvider("dataProviderFalsy")
  public void testExtractorNotSampled(final String value) {
    assertThat(EXTRACTOR.extract(Collections.singletonMap("ot-tracer-sampled", value)))
        .isEqualTo(TraceContextOrSamplingFlags.create(false, false));
  }

  @DataProvider
  public static String[] dataProviderTruthy() {
    return new String[] {"1", "true", "True"};
  }

  @Test
  @UseDataProvider("dataProviderTruthy")
  public void testExtractorSampled(final String value) {
    assertThat(EXTRACTOR.extract(Collections.singletonMap("ot-tracer-sampled", value)))
        .isEqualTo(TraceContextOrSamplingFlags.create(true, false));
  }

  @Test
  public void testExtractor() {
    final Map<String, String> carrier = new HashMap<>();
    carrier.put("ot-tracer-traceid", ENCODED_TRACE_ID);
    carrier.put("ot-tracer-spanid", ENCODED_SPAN_ID);
    carrier.put("ot-tracer-sampled", "1");
    assertThat(EXTRACTOR.extract(carrier))
        .isEqualTo(TraceContextOrSamplingFlags.create(SAMPLED_CONTEXT));
  }

  @Test
  public void testExtractorMalformed() {
    final Map<String, String> carrier = new HashMap<>();
    carrier.put("ot-tracer-traceid", "");
    carrier.put("ot-tracer-spanid", "");
    carrier.put("ot-tracer-sampled", "1");
    assertThat(EXTRACTOR.extract(carrier)).isEqualTo(TraceContextOrSamplingFlags.EMPTY);
  }
}
