package ru.ksu.niimm.cll.uima.morph.opencorpora.model;

import java.io.Serializable;
import java.util.BitSet;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Function;

import ru.ksu.niimm.cll.uima.morph.opencorpora.resource.MorphDictionary;

/**
 * @author Rinat Gareev (Kazan Federal University)
 * 
 */
public class Wordform implements Serializable {

	private static final long serialVersionUID = -8435061415886880938L;
	private static final BitSet EMPTY_BITSET = new BitSet();

	public static Builder builder(MorphDictionary dict, int lemmaId) {
		return new Builder(dict, lemmaId);
	}

	public static class Builder {
		private Wordform instance = new Wordform();
		private MorphDictionary dict;

		public Builder(MorphDictionary dict, int lemmaId) {
			instance.lemmaId = lemmaId;
			this.dict = dict;
		}

		public Builder addGrammeme(String gramId) {
			if (instance.grammems == null) {
				instance.grammems = new BitSet(dict.getGrammemMaxNumId() + 1);
			}
			int gramNumId = dict.getGrammemNumId(gramId);
			instance.grammems.set(gramNumId);
			return this;
		}

		public Wordform build() {
			if (instance.grammems == null) {
				instance.grammems = EMPTY_BITSET;
			}
			return instance;
		}
	}

	public static BitSet getAllGramBits(Wordform wf, MorphDictionary dict) {
		BitSet bs = wf.getGrammems();
		// TODO how to detect null lemma ids?
		bs.or(dict.getLemma(wf.getLemmaId()).getGrammems());
		return bs;
	}

	public static Function<Wordform, BitSet> allGramBitsFunction(final MorphDictionary dict) {
		return new Function<Wordform, BitSet>() {
			@Override
			public BitSet apply(Wordform arg) {
				return getAllGramBits(arg, dict);
			}
		};
	}

	private int lemmaId;
	private BitSet grammems;

	private Wordform() {
	}

	public Wordform(int lemmaId, BitSet grammems) {
		this.lemmaId = lemmaId;
		this.grammems = grammems;
	}

	public int getLemmaId() {
		return lemmaId;
	}

	public Wordform cloneWithLemmaId(int lemmaId) {
		return new Wordform(lemmaId, grammems);
	}

	public Wordform cloneWithGrammems(BitSet grammems) {
		Wordform result = new Wordform();
		result.lemmaId = this.lemmaId;
		result.grammems = grammems;
		return result;
	}

	public BitSet getGrammems() {
		// !!!
		return (BitSet) grammems.clone();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(lemmaId).append(grammems)
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Wordform)) {
			return false;
		}
		Wordform that = (Wordform) obj;
		return new EqualsBuilder().append(this.lemmaId, that.lemmaId)
				.append(this.grammems, that.grammems).isEquals();
	}

}