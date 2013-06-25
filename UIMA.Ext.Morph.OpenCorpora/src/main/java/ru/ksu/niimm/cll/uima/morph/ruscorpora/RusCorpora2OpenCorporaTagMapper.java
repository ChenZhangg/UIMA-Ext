/**
 * 
 */
package ru.ksu.niimm.cll.uima.morph.ruscorpora;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.opencorpora.cas.Wordform;

import ru.kfu.itis.cll.uima.cas.FSUtils;
import static ru.ksu.niimm.cll.uima.morph.opencorpora.model.MorphConstants.*;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

/**
 * @author Rinat Gareev (Kazan Federal University)
 * 
 */
public class RusCorpora2OpenCorporaTagMapper implements RusCorporaTagMapper {

	@Override
	public void mapFromRusCorpora(RusCorporaWordform srcWf, Wordform targetWf) {
		JCas jCas;
		try {
			jCas = targetWf.getCAS().getJCas();
		} catch (CASException e) {
			throw new RuntimeException(e);
		}
		if (srcWf.getLex() != null) {
			targetWf.setLemma(srcWf.getLex());
		}
		WordformBuilder wb = new WordformBuilder();
		// pos
		{
			Submapper posMapper = subMappers.get(srcWf.getPos());
			if (posMapper == null) {
				throw new IllegalStateException(String.format(
						"Unhandled pos: %s", srcWf.getPos()));
			}
			posMapper.map(srcWf, wb);
		}
		//
		Set<String> srcGrams = Sets.union(srcWf.getLexGrammems(), srcWf.getWordformGrammems());
		for (String srcGr : srcGrams) {
			Submapper grMapper = subMappers.get(srcGr);
			if (grMapper == null) {
				throw new IllegalStateException(String.format(
						"Unhandled tag: %s", srcGr));
			}
			grMapper.map(srcWf, wb);
		}
		//
		targetWf.setPos(wb.pos);
		targetWf.setGrammems(FSUtils.toStringArray(jCas, wb.grammems));
	}

	private static final Submapper adjMapper = new Submapper() {
		@Override
		public void map(RusCorporaWordform srcWf, WordformBuilder wb) {
			if (srcWf.getAllGrammems().contains("brev")) {
				wb.pos = ADJS;
			} else {
				wb.pos = ADJF;
			}
		}
	};

	private static final Submapper verbMapper = new Submapper() {
		@Override
		public void map(RusCorporaWordform srcWf, WordformBuilder wb) {
			Set<String> srcGrams = srcWf.getAllGrammems();
			if (srcGrams.contains("inf")) {
				wb.pos = INFN;
			} else if (srcGrams.contains("partcp")) {
				if (srcGrams.contains("brev")) {
					wb.pos = PRTS;
				} else {
					wb.pos = PRTF;
				}
			} else if (srcGrams.contains("ger")) {
				wb.pos = GRND;
			} else {
				wb.pos = VERB;
			}
		}
	};

	private static Submapper gramTag(final String... grs) {
		if (grs.length == 0) {
			throw new IllegalArgumentException();
		}
		return new Submapper() {
			@Override
			public void map(RusCorporaWordform srcWf, WordformBuilder wb) {
				wb.grammems.addAll(Arrays.asList(grs));
			}
		};
	}

	private static Submapper pos(final String pos) {
		return new Submapper() {
			@Override
			public void map(RusCorporaWordform srcWf, WordformBuilder wb) {
				wb.pos = pos;
			}
		};
	}

	private static Submapper and(final Submapper... ms) {
		if (ms.length == 0) {
			throw new IllegalArgumentException();
		}
		return new Submapper() {
			@Override
			public void map(RusCorporaWordform srcWf, WordformBuilder wb) {
				for (Submapper nested : ms) {
					nested.map(srcWf, wb);
				}
			}
		};
	}

	private static final Submapper noOp = new Submapper() {
		@Override
		public void map(RusCorporaWordform srcWf, WordformBuilder wb) {
		}
	};

	private static interface Submapper {
		void map(RusCorporaWordform srcWf, WordformBuilder wb);
	}

	private static class WordformBuilder {
		private String pos;
		private final Set<String> grammems = Sets.newLinkedHashSet();
	}

	private static final Map<String, Submapper> subMappers = ImmutableMap
			.<String, Submapper> builder()
			.put("S", pos(NOUN))
			.put("A", adjMapper)
			.put("NUM", pos(NUMR))
			.put("A-NUM", and(pos(ADJF), gramTag(Anum)))
			.put("ANUM", and(pos(ADJF), gramTag(Anum)))
			.put("V", verbMapper)
			.put("ADV", pos(ADVB))
			.put("PRAEDIC", pos(PRED))
			.put("PARENTH", gramTag(Prnt))
			.put("S-PRO", pos(NPRO))
			.put("A-PRO", and(pos(ADJF), gramTag(Apro)))
			.put("ADV-PRO", and(pos(ADVB), gramTag(Apro)))
			.put("PRAEDIC-PRO", pos(NPRO))
			.put("PR", pos(PREP))
			.put("CONJ", pos(CONJ))
			.put("PART", pos(PRCL))
			.put("INTJ", pos(INTJ))
			.put("NONLEX", noOp)
			//
			.put("m", gramTag(masc))
			.put("f", gramTag(femn))
			.put("m-f", gramTag(GNdr, comgend))
			.put("n", gramTag(neut))
			//
			.put("anim", gramTag(anim))
			.put("inan", gramTag(inan))
			//
			.put("sg", gramTag(sing))
			.put("pl", gramTag(plur))
			//
			.put("nom", gramTag(nomn))
			.put("gen", gramTag(gent))
			.put("dat", gramTag(datv))
			.put("dat2", gramTag(datv))
			.put("acc", gramTag(accs))
			.put("ins", gramTag(ablt))
			.put("loc", gramTag(loct))
			.put("gen2", gramTag(gen2))
			.put("acc2", gramTag(acc2))
			.put("loc2", gramTag(loc2))
			.put("voc", gramTag(voct))
			.put("adnum", gramTag(gent))
			//
			.put("comp", pos(COMP))
			.put("comp2", pos(COMP))
			.put("supr", gramTag(Supr))
			//
			.put("brev", noOp)
			.put("plen", noOp)
			//
			.put("pf", gramTag(perf))
			.put("ipf", gramTag(impf))
			//
			.put("intr", gramTag(intr))
			.put("tran", gramTag(tran))
			//
			.put("act", gramTag(actv))
			.put("pass", gramTag(pssv))
			.put("med", gramTag(actv, pssv))
			//
			.put("inf", noOp)
			.put("partcp", noOp)
			.put("ger", noOp)
			//
			.put("indic", gramTag(indc))
			.put("imper", gramTag(impr))
			.put("imper2", gramTag(impr))
			// 
			.put("praet", gramTag(past))
			.put("praes", gramTag(pres))
			.put("fut", gramTag(futr))
			//
			.put("1p", gramTag(per1))
			.put("2p", gramTag(per2))
			.put("3p", gramTag(per3))
			//
			.put("persn", gramTag(Name))
			.put("patrn", gramTag(Patr))
			.put("famn", gramTag(Surn))
			.put("zoon", noOp)
			.put("0", gramTag(Fixd))
			//
			.put("distort", gramTag(Dist))
			.put("anom", noOp)
			.put("ciph", noOp)
			.put("INIT", noOp)
			.put("abbr", gramTag(Abbr))
			.put("obsc", noOp)
			//
			.build();
}