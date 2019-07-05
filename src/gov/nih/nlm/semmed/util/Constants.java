/**
 *
 */
package gov.nih.nlm.semmed.util;

import java.util.Arrays;
import java.util.List;

/**
 * @author hkilicoglu
 *
 */
public class Constants {

//	private static final String[] ANATOMY_SEMTYPES = {"anst", "blor", "bpoc", "bsoj", "cell", "celc", "emst", "ffas", "gngm", "tisu"};
	private static final String[] TREAT_ANAT_SEMTYPES = {"anst", "blor", "bpoc", "bsoj", "ffas"};
	private static final String[] DRUG_ANAT_SEMTYPES = {"anst", "bpoc", "cell", "celc", "emst", "ffas", "gngm", "tisu"};
	private static final String[] GENE_ANAT_SEMTYPES = {"anst", "bpoc", "cell", "celc", "emst", "ffas", "gngm", "neop", "tisu"};
	private static final String[] CHEM_SEMTYPES = {"aapp","antb", "bacs", "carb", "eico", "elii", "gngm", "hops", "horm", "imft", "lipd", "opco", "orch", "phsu", "strd", "topp", "vita"};
	private static final String[] DIAGNOSIS_SEMTYPES = {"diap", "lbpr", "sosy"};
	private static final String[] DISORDER_SEMTYPES = {"acab", "anab", "cgab", "comd", "dsyn", "inpo", "mobd", "neop", "patf", "sosy"};

	private static final String[] DRUG_CHEM_SEMTYPES = {"aapp", "antb", "bacs", "carb", "eico", "elii", "enzy", "gngm", "hops", "horm",
		"imft", "inch", "lipd", "nnon", "nsba", "opco", "orch", "phsu", "strd", "vita"};

	private static final String[] DRUG_SEMTYPES = {"aapp", "antb", "gngm", "hops", "horm", "nnon", "orch", "phsu", "strd", "vita"};
	private static final String[] ETIOLOGY_SEMTYPES = {"aapp", "antb", "bact", "bacs", "elii", "fngs", "gngm", "hops",
		 "imft", "invt", "opco", "rich", "virs"};
	private static final String[] HUMAN_SEMTYPES = {"aggp", "famg", "grup", "humn", "podg", "popg", "prog"};
	private static final String[] ORG_CAUSE_SEMTYPES = {"bact", "fngs", "invt", "rich", "virs"};
	private static final String[] ORG_SEMTYPES = {"aggp", "anim", "arch", "bact", "famg", "fngs", "grup", "humn", "invt", "mamm", "orgm", "podg", "popg", "prog", "vtbt"};
	// Dongwook, 4/7/2008 LIVINGBEING type definition is added to make the same as in summarize_pharmgen.pl
	private static final String[] LIVINGBEING_SEMTYPES = {"anim", "arch", "bact", "fngs", "humn", "invt", "mamm", "orgm", "podg", "popg", "vtbt"};
	private static final String[] PHYSIOLOGY_SEMTYPES = {"biof", "celf", "comd", "genf", "menp", "moft", "orgf", "ortf", "phsf"};
	private static final String[] PROCESS_SEMTYPES = {"acab", "anab", "biof", "celf", "cgab", "comd", "dsyn", "inpo", "menp", "mobd", "moft", "neop", "npop", "orgf", "ortf", "phsf", "patf", "sosy"};
	private static final String[] SUBSTANCE_SEMTYPES = {"aapp", "antb", "bacs", "carb", "eico", "elii", "enzy", "gngm", "hops", "horm",
		"imft", "inch", "lipd", "nsba", "nusq", "opco", "orch", "phsu", "rcpt", "strd", "vita"};
	private static final String[] TREATMENT_SEMTYPES = {"aapp", "antb", "carb", "eico", "gngm", "horm", "lipd", "orch", "phsu", "strd", "topp", "vita"};


	public static final List<String> NHLBI_DISORDER_SEMGROUP = Arrays.asList("acab", "anab", "cgab", "comd", "dsyn", "inpo",
            "mobd", "neop", "patf", "sosy","fndg");
	public static final List<String> NHLBI_CHEM_SEMGROUP = Arrays.asList("aapp","antb","bacs","carb","eico","elii",
			"gngm","hops","horm","imft","lipd","opco","orch","phsu","strd","vita","fndg","dsyn","sosy","enzy","lbtr","patf","dora","food","inbe","clna");
	public static final List<String> NHLBI_TREAT_SEMGROUP = Arrays.asList("aapp", "antb", "carb", "eico", "gngm", "horm", "lipd", "orch", "phsu", "strd",
			"topp", "vita","hlca", "dora", "edac");


	public static final List<String> HUMAN_SEMGROUP = Arrays.asList(HUMAN_SEMTYPES);
	public static final List<String> TREAT_ANAT_SEMGROUP = Arrays.asList(TREAT_ANAT_SEMTYPES);
	public static final List<String> DRUG_ANAT_SEMGROUP = Arrays.asList(DRUG_ANAT_SEMTYPES);
	public static final List<String> GENE_ANAT_SEMGROUP = Arrays.asList(GENE_ANAT_SEMTYPES);
	public static final List<String> CHEM_SEMGROUP = Arrays.asList(CHEM_SEMTYPES);
	public static final List<String> DIAGNOSIS_SEMGROUP = Arrays.asList(DIAGNOSIS_SEMTYPES);
	public static final List<String> DISORDER_SEMGROUP = Arrays.asList(DISORDER_SEMTYPES);
	public static final List<String> DRUG_SEMGROUP = Arrays.asList(DRUG_SEMTYPES);
	public static final List<String> DRUG_CHEM_SEMGROUP = Arrays.asList(DRUG_CHEM_SEMTYPES);
	public static final List<String> ETIOLOGY_SEMGROUP = Arrays.asList(ETIOLOGY_SEMTYPES);
	public static final List<String> ORG_CAUSE_SEMGROUP = Arrays.asList(ORG_CAUSE_SEMTYPES);
	public static final List<String> ORG_SEMGROUP = Arrays.asList(ORG_SEMTYPES);
	public static final List<String> LIVINGBEING_SEMGROUP = Arrays.asList(LIVINGBEING_SEMTYPES);
	public static final List<String> PHYSIOLOGY_SEMGROUP = Arrays.asList(PHYSIOLOGY_SEMTYPES);
	public static final List<String> PROCESS_SEMGROUP = Arrays.asList(PROCESS_SEMTYPES);
	public static final List<String> SUBSTANCE_SEMGROUP = Arrays.asList(SUBSTANCE_SEMTYPES);
	public static final List<String> TREATMENT_SEMGROUP = Arrays.asList(TREATMENT_SEMTYPES);

	// should be removed eventually - 2006AA
	private static String[] NOVELTY_FILTER_CONCEPTS = {"agonists", "Elements", "Symptoms", "Therapeutic procedure"};
	private static String[] NOVELTY_FILTER_GENOMIC_CONCEPTS = {"agonists", "Arm of chromosome", "Base Sequence", "Binding Protein",
		"Cancer-Predisposing Gene", "Candidate Disease Gene", "Chromosome Fragile Sites", "Consensus Sequence", "Conserved Sequence",
		"Dependent", "DNA Library", "DNA Sequence", "Elements", "Functional disorder", "Gene Family", "Gene Structure", "Genes",
		"Genes, Suppressor", "Genetic Materials", "Genome", "glycoproteins", "Histones", "Homo sapiens", "Homologous Gene",
		"House mice", "Human", "Inactivation", "Individual", "Integrons", "mammals", "Membrane Protein Gene", "Mice, Transgenic",
		"Microtubule-Associated Proteins", "Nucleosomes", "Obstruction", "Population Group", "Proteins", "Proto-Oncogenes",
		"Pseudogenes", "Quantitative Trait Loci", "Rattus", "Regulatory Element", "Receptors, Nuclear", "Repetitive Region",
		"Response Elements", "Rodent", "Single Nucleotide Polymorphism", "Structural gene", "Symptoms", "Therapeutic procedure",
		"TRANSCRIPTION FACTOR", "Transgenes", "Translocation Breakpoint", "Tumor Suppressor Genes", "Voluntary Workers"};
	public static final List<String> NOVELTY_FILTER_CONCEPT_LIST = Arrays.asList(NOVELTY_FILTER_CONCEPTS);
	public static final List<String> NOVELTY_FILTER_GENOMIC_CONCEPT_LIST = Arrays.asList(NOVELTY_FILTER_GENOMIC_CONCEPTS);

	public static final String[] DEMOTED_PREDICATES = {Constants.PROCESS_OF};
	public static final List DEMOTED_PREDICATE_LIST = Arrays.asList(DEMOTED_PREDICATES);

	// Dongwook, 04/08/2008
	// Added new Dominant_predicate list for Internaction and Pharmacogenomics saliency
	public static final String[] INTERPHARMA_DEMOTED_PREDICATES = {Constants.TREATS, Constants.ISA, Constants.PROCESS_OF};
	public static final List INTERPHARMA_DEMOTED_PREDICATE_LIST = Arrays.asList(INTERPHARMA_DEMOTED_PREDICATES);

	public static final String ADMINISTERED_TO = "ADMINISTERED_TO";
	public static final String AFFECTS = "AFFECTS";
	public static final String ASSOCIATED_WITH = "ASSOCIATED_WITH";
	public static final String AUGMENTS = "AUGMENTS";
	public static final String CAUSES = "CAUSES";
	public static final String COEXISTS_WITH = "COEXISTS_WITH";
	public static final String CONVERTS_TO = "CONVERTS_TO";
	public static final String COMPLICATES = "COMPLICATES";
	public static final String DIAGNOSES = "DIAGNOSES";
	public static final String DISRUPTS = "DISRUPTS";
	public static final String INHIBITS = "INHIBITS";
	public static final String INTERACTS_WITH = "INTERACTS_WITH";
	public static final String ISA = "ISA";
	public static final String LOCATION_OF = "LOCATION_OF";
	public static final String MANIFESTATION_OF = "MANIFESTATION_OF";
	public static final String PART_OF = "PART_OF";
	public static final String PREDISPOSES = "PREDISPOSES";
	public static final String PREVENTS = "PREVENTS";
	public static final String PROCESS_OF = "PROCESS_OF";
	public static final String STIMULATES = "STIMULATES";
	public static final String TREATS = "TREATS";

	public static final String DEF_FILE = "/config/semmeddefs.xml";
	//temp
	public static final String SCHIZO_TREAT_FILE = "/config/schizo-treatment-summary.xml";
	public static final String PARKINSON_TREAT_FILE = "/config/parkinson-treatment-summary.xml";
	public static final String PARKINSON_GENETIC_FILE = "/config/parkinson-genetic-summary.xml";
	public static final String AIDS_FILE = "/config/aids.xml";
	public static final String ALZHEIMER_FILE = "/config/alzheimer.xml";
	public static final String ASTHMA_FILE = "/config/asthma.xml";
	public static final String ATHEROSCLEROSIS_FILE = "/config/atherosclerosis.xml";
	public static final String BREASTCA_FILE = "/config/breastca.xml";
	public static final String CROHN_FILE = "/config/crohn.xml";
	public static final String DEPRESSION_FILE = "/config/depression.xml";
	public static final String OBESITY_FILE = "/config/obesity.xml";
	public static final String PARKINSON_FILE = "/config/parkinson.xml";
	public static final String SCHIZOPHRENIA_FILE = "/config/schizophrenia.xml";
	public static final String ANTI_INFLAMMATORY_FILE = "/config/antiinflammatory.xml";

	public static final String SCHIZO_MEDLINE_SEARCH_FILE = "/config/schizo-medline-search-results.xml";
	public static final String ALZHEIMER_MEDLINE_SEARCH_FILE = "/config/alzheimer-medline-search-results.xml";
	public static final String INFLAM_CTRIALS_SEARCH_FILE = "/config/antiinflammatory-ctrials-search-results.xml";

	public static final String EUTILS = "http://www.ncbi.nlm.nih.gov/entrez/eutils";
	public static final String EUTILS_PATTERN = "<Count>(\\d+)</Count>.*<QueryKey>(\\d+)</QueryKey>.*<WebEnv>(\\S+)</WebEnv>";
	public static final String QUERY_DEFAULT_LIMITS = " AND hasabstract[text] AND English[Lang] AND "; //TODO replace spaces by '+' [Alejandro]
	public static final String QUERY_WITH_ABSTRACT_ENGLISH = " AND hasabstract[text] AND English[Lang] "; // [Dongwook]

	public static final String ESSIE_HOST = "clinicaltrials.gov";
	public static final int ESSIE_PORT = 451;
}
