package adaa.analytics.rules.logic.representation;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;

import adaa.analytics.rules.logic.induction.ActionCovering;

public class RuleSerializer {

	protected ExampleSet set;
	protected char sep;
	protected String nullString;
	
	public RuleSerializer(ExampleSet dataset, char seperator, String nullIndicator) {
		set = dataset;
		sep = seperator;
		nullString = nullIndicator;
	}
	
	protected class StringBuilderBuilder {
		
		private StringBuilder builder;
		private char sep;
		
		public StringBuilderBuilder (StringBuilder builder, char seperator) {
			
			this.builder = builder;
			sep = seperator;
		}
		
		public <T> StringBuilderBuilder append(T data) {
			
			builder.append(data);
			builder.append(sep);
			return this;
		}
		
		public String build() {
			return builder.toString();
		}
		
	}
	
	public String serializeToCsv(ActionRuleSet ruleset) {
		
		List<String> rules = ruleset.getRules()
			.stream()
			.map(x -> this.serializeToCsv((ActionRule)x))
			.collect(Collectors.toList());
		
		rules.add(0, generateHeader());
		
		return String.join("\n", rules);		
	}
	
	private String generateHeader() {
		StringBuilderBuilder builder = new StringBuilderBuilder(new StringBuilder(), sep);
		
		Iterator<Attribute> iter = set.getAttributes().allAttributes();
		
		builder
			.append("pl")
			.append("pr")
			.append("nl")
			.append("nr")
			.append("P")
			.append("N");
		
		while (iter.hasNext()) {
			
			Attribute atr = iter.next();
			
			builder
				.append(atr.getName() + "_L")
				.append(atr.getName() + "_P");
			
		}
		return builder.build();
	}

	public String serializeToCsv(ActionRule rule) {
		
		StringBuilderBuilder builder = new StringBuilderBuilder(new StringBuilder(), sep);
		
		ActionCovering cov = (ActionCovering) rule.getCoveringInformation();
		
		builder
			.append(cov.weighted_p)
			.append(cov.weighted_pRight)
			.append(cov.weighted_n)
			.append(cov.weighted_nRight)
			.append(cov.weighted_P)
			.append(cov.weighted_N);
		
		Iterator<Attribute> iter = set.getAttributes().allAttributes();
		List<Action> conds = rule.getPremise().getSubconditions().stream().map(Action.class::cast).collect(Collectors.toList());
		
		while (iter.hasNext()) {
			
			Attribute atr = iter.next();
			
			Stream<Action> s = conds.stream().filter(x -> x.getAttribute() == atr.getName());
			
			Optional<Action> act = s.findFirst();
			
			if (act.isPresent()) {
				
				Action action = act.get();
				
				builder
					.append(action.getLeftValue() == null ? nullString : action.getLeftValue())
					.append(action.getRightValue() == null ? nullString : action.getRightValue());
				
			} else {
				builder.append(nullString).append(nullString);
			}
			
		}
		
		return builder.build();
	}

}