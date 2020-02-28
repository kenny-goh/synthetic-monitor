package com.gkh.syntheticmonitor.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.gkh.syntheticmonitor.exception.SyntheticTestException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Data
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class SyntheticTest {

	private String name;

	@Value("${apitest.scheduleTimeInMinute}")
	private int scheduleTimeInMinutes;

	@Value("${apitest.pauseTimeBetweenActionsInSeconds}")
	private int pauseTimeBetweenActionsInSeconds;

	private List<AbstractSyntheticTestAction> actions;

	public static class SyntheticTestBuilder {

		public SyntheticTestBuilder getApiAction(String name,
		                                         String url,
		                                         HashMap headers,
		                                         String preRequestScript,
		                                         String postRequestScript) {
			this.initActionsIfApplicable();
			this.actions.add(SyntheticTestActionAPI.builder()
					.name(name)
					.requestMethod(SyntheticTestActionAPI.METHOD_GET)
					.requestUrl(url)
					.requestHeaders(headers)
					.preRequestScript(preRequestScript)
					.postRequestScript(postRequestScript)
					.build());
			return this;
		}

		public SyntheticTestBuilder getApiAction(String name,
		                                         String url,
		                                         String preRequestScript,
		                                         String postRequestScript) {
			this.getApiAction(name, url, new HashMap<>(), preRequestScript, postRequestScript);
			return this;
		}

		public SyntheticTestBuilder getApiAction(String name,
		                                         String url) {
			this.getApiAction(name, url, new HashMap<>(), "", "");
			return this;
		}


		public SyntheticTestBuilder postApiAction(String name,
		                                          String url,
		                                          HashMap headers,
		                                          String body,
		                                          String preRequestScript,
		                                          String postRequestScript) {
			this.initActionsIfApplicable();
			this.actions.add(SyntheticTestActionAPI.builder()
					.name(name)
					.requestMethod(SyntheticTestActionAPI.METHOD_POST)
					.requestUrl(url)
					.requestHeaders(headers)
					.requestBody(body)
					.preRequestScript(preRequestScript)
					.postRequestScript(postRequestScript)
					.build());
			return this;
		}

		public SyntheticTestBuilder postApiAction(String name, String url, HashMap headers, String body) {
			this.postApiAction(name, url, headers, body, "","");
			return this;
		}

		private void initActionsIfApplicable() {
			if (this.actions == null) {
				this.actions = new ArrayList<>();
			}
		}
	}

	public void execute(TestExecutionContext context) throws SyntheticTestException {
		context.getSyntheticTestResult().setName(this.name);
		var size = actions.size();
		for (var each : actions) {
			log.debug("Executing action {}", each.getName());
				each.preExecuteScript(context);
			each.resolveVariables(context);
				each.execute(context);
				each.postExecuteScript(context);
				var index = actions.indexOf(each);
				if (index < size - 1) {
					try {
						TimeUnit.SECONDS.sleep(this.pauseTimeBetweenActionsInSeconds);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
		}
	}

	public static String toYAML(SyntheticTest syntheticTest) throws IOException {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		String result = mapper.writeValueAsString(syntheticTest);
		return result;
	}

	public static SyntheticTest fromYAML(InputStream stream) throws IOException {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		SyntheticTest result = mapper.readValue(stream, SyntheticTest.class);
		return result;
	}


	public static String toYAML2(SyntheticTest syntheticTest) {
		Representer representer = new Representer();
		representer.addClassTag(SyntheticTest.class, new Tag("!SyntheticTest"));
		representer.addClassTag(SyntheticTestActionAPI.class, new Tag("!ActionAPI"));
		representer.addClassTag(SyntheticTestActionNOOP.class, new Tag("!ActionNOOP"));

		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setPrettyFlow(true);

		Yaml yaml = new Yaml(representer, options);
		String result = yaml.dump(syntheticTest);
		return result;
	}


	public static SyntheticTest fromYAML2(InputStream stream) throws IOException {
		Constructor constructor = new Constructor();
		constructor.addTypeDescription(new TypeDescription(SyntheticTest.class, "!SyntheticTest"));
		constructor.addTypeDescription(new TypeDescription(SyntheticTestActionAPI.class, "!ActionAPI"));
		constructor.addTypeDescription(new TypeDescription(SyntheticTestActionNOOP.class, "!NOOP"));

		Yaml yaml = new Yaml(constructor);

		return (SyntheticTest) yaml.load(stream);
	}

}
