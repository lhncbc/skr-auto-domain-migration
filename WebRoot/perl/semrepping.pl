#!/usr/bin/perl

# Insert SemRep predications to the Semantic Medline db.
# Input file should have full-fielded output format (-D).
# dbname is currently semmed2006.

use DBI;

$dbname = shift @ARGV;
chomp $dbname;
$input_file = shift @ARGV;
chomp $input_file;

%seen_concs = ();
%seen_predications = ();
%seen_interventions = ();
%seen_predids = ();

$type = "semrep";

$dbh = DBI->connect("dbi:mysql:database=" . $dbname . ";host=indsrv2:3306;user=root;password=indsrv1\@root") or die "Couldn't connect to database: " . DBI->errstr;

$insert_sentence_sth = $dbh->prepare_cached('INSERT INTO SENTENCE (PMID,TYPE,NUMBER,SENTENCE) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE SENTENCE=?') or die "Couldn't prepare statement: " . $dbh->errstr;

$insert_predication_sth = $dbh->prepare_cached('INSERT INTO PREDICATION (PREDICATE, TYPE) VALUES (?,?)') or die "Couldn't prepare statement: " . $dbh->errstr;

$insert_predication_argument_sth = $dbh->prepare_cached('INSERT IGNORE INTO PREDICATION_ARGUMENT (PREDICATION_ID,CONCEPT_SEMTYPE_ID,TYPE) VALUES (?,?,?)') or die "Couldn't prepare statement: " . $dbh->errstr;

$insert_sentence_predication_sth = $dbh->prepare_cached('INSERT INTO SENTENCE_PREDICATION (SENTENCE_ID,PREDICATION_ID, PREDICATION_NUMBER, SUBJECT_DIST, SUBJECT_MAXDIST, SUBJECT_START_INDEX, SUBJECT_END_INDEX, SUBJECT_TEXT, SUBJECT_SCORE, INDICATOR_TYPE, PREDICATE_START_INDEX, PREDICATE_END_INDEX, OBJECT_DIST, OBJECT_MAXDIST, OBJECT_START_INDEX, OBJECT_END_INDEX, OBJECT_TEXT, OBJECT_SCORE) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)') or die  "Couldn't prepare statement: " . $dbh->errstr;

$select_pred_argument_sth = $dbh->prepare_cached('SELECT P.PREDICATION_ID FROM PREDICATION P, PREDICATION_ARGUMENT PA WHERE P.PREDICATE=? AND P.TYPE=? AND PA.PREDICATION_ID=P.PREDICATION_ID AND PA.CONCEPT_SEMTYPE_ID=? AND PA.TYPE=?');

$select_concept_sth = $dbh->prepare_cached('SELECT CS.CONCEPT_SEMTYPE_ID FROM CONCEPT_SEMTYPE CS, CONCEPT C WHERE C.CUI=? AND CS.SEMTYPE=? AND C.CONCEPT_ID=CS.CONCEPT_ID') or die "Couldn't prepare statement: " . $dbh->errstr;

$select_concept_all_sth = $dbh->prepare_cached('SELECT CS.CONCEPT_SEMTYPE_ID, CS.SEMTYPE FROM CONCEPT_SEMTYPE CS, CONCEPT C WHERE C.CUI=? AND C.CONCEPT_ID=CS.CONCEPT_ID') or die "Couldn't prepare statement: " . $dbh->errstr;

$select_concept_ct_sth = $dbh->prepare_cached('SELECT C.CUI FROM CONCEPT C WHERE C.TYPE=\'CT\' AND C.PREFERRED_NAME=?') or die "Couldn't prepare statement: " . $dbh->errstr;

$select_humn_sth = $dbh->prepare_cached('SELECT CS.CONCEPT_SEMTYPE_ID FROM CONCEPT_SEMTYPE CS, CONCEPT C WHERE C.CUI=? AND CS.SEMTYPE IN (\'popg\',\'podg\', \'prog\', \'famg\', \'grup\', \'aggp\') AND C.CONCEPT_ID=CS.CONCEPT_ID') or die "Couldn't prepare statement: " . $dbh->errstr;

$select_sentence_sth = $dbh->prepare_cached('SELECT SENTENCE_ID FROM SENTENCE WHERE PMID=? AND TYPE=? AND NUMBER=?') or die "Couldn't prepare statement: " . $dbh->errstr;

$delete_predication_sth = $dbh->prepare_cached('DELETE SP FROM SENTENCE_PREDICATION SP, PREDICATION P, SENTENCE S WHERE S.PMID=? AND P.TYPE=? AND SP.PREDICATION_ID=P.PREDICATION_ID AND S.SENTENCE_ID=SP.SENTENCE_ID') or die "Couldn't prepare statement: " . $dbh->errstr;

$select_pa_count_sth = $dbh->prepare_cached('SELECT COUNT(*) FROM PREDICATION_ARGUMENT WHERE PREDICATION_ID=?') or die "Couldn't prepare statement: " . $dbh->errstr;

$select_pmid_sentence_sth = $dbh->prepare_cached('SELECT COUNT(*) FROM SENTENCE WHERE PMID = ?') or die "Couldn't prepare statement: " . $dbh->errstr;

$insert_title_sth = $dbh->prepare_cached('INSERT INTO TITLES (PMID,TITLE) VALUES(?,?)') or die "Couldn't prepare statement: " . $dbh->errstr;

$select_title_sth = $dbh->prepare_cached('SELECT COUNT(*) FROM TITLES WHERE PMID = ?') or die "Couldn't prepare statement: " . $dbh->errstr;


open(F,"<:utf8",$input_file) or die("Can not open the $input_file file!");
# needed for unicode characters
$dbh->do('SET NAMES utf8');

$num_sentetce_pmid = 0;
while (<F>) {
    my($line) = $_;
    chomp($line);
    @line_elements = split(/\|/, $line);
    if ($line_elements[0] eq "SE") {
      $pmid = $line_elements[1];
      $senttype = $line_elements[3];
      $number = $line_elements[4];
      # ensure that if predications already exist in the database for the pmid, they are removed, as we are probably trying to update the predications
      unless ($prev_pmid eq $pmid) {
	$delete_predication_sth->execute($pmid,$type) or die "Couldn't execute statement: " + $delete_predication_sth->errstr;
      }

      $select_pmid_sentence_sth->execute($pmid) or die "Couldn't execute statement: " + $select_pmid_sentence_sth->errstr;
      $num_sentence_pmid = $select_pmid_sentence_sth->fetchrow_array();
      # if the number of sentences in the database is greater than zero, skip the following
      if($num_sentece_pmid > 0)
      	{ next; }

      # get the sentence_id in the db for the given sentence.
      $select_sentence_sth->execute($pmid,$senttype,$number) or die "Couldn't execute statement: " + $select_sentence_sth->errstr;
      $sentence_id = $select_sentence_sth->fetchrow_array();

      # we are looking at the text line. Insert or update the sentence record.
      if ($line_elements[5] eq "text") {
	$sentence = $line_elements[6];
	$insert_sentence_sth->execute($pmid,$senttype,$number,$sentence,$sentence) or die "Couldn't execute statement: " . $sth->errstr;
        # inserting title into TITLE table

        if($line_elements[3] eq "ti") {
        	$select_title_sth->execute($pmid) or die "Couldn't execute statement: " + $select_sentence_sth->errstr;
      		$num_title = $select_title_sth->fetchrow_array();

      		# if the number of titles in the database is zero, skip the following
      		if($num_title <= 0) {
            		$insert_title_sth->execute($pmid, $sentence) or die "Couldn't execute statement: " . $sth->errstr;
                }
        }
      }
      # we are ignoring the entities, and only looking at the predication lines.
      elsif ($line_elements[5] eq "relation") {
	# print "$line\n";
	@subj_uis = ();
	@obj_uis = ();
	@obj_semtypes = ();
	@subj_semtypes = ();

	undef $subj_text;
	undef $obj_text;
	undef $obj_ui;
	undef $obj_semtype;
	undef $subj_semtype;
	undef $subj_concsem_id;
	undef $obj_concsem_id;

	$subj_maxdist = $line_elements[6];
	$subj_dist = $line_elements[7];
	$subj_cui = $line_elements[8];
	$subj_meta_name = $line_elements[9];
	@subj_semtypes = split(/,/, $line_elements[10]);
	$subj_semtype = $line_elements[11];
	$subj_entrez_id = $line_elements[12];
	$subj_entrez_name = $line_elements[13];
	$subj_text = $line_elements[14];
	$subj_score = $line_elements[18];
	$subj_start_ind = $line_elements[19];
	$subj_end_ind = $line_elements[20];

	# Extract the id for the given subject from the db.
	@subj_uis = &ReadArgumentUIs($subj_meta_name,
				     $subj_cui,
				     $subj_semtype,
				     $subj_entrez_id,
				     $subj_entrez_name);

	# this used to handle the "None" arguments, but they
	# should not really occur anymore. Still keeping it here
	# to be on the safe side.
        if ((scalar @subj_uis) == 0) {next;}

	# read predicate data
	$indicator_type= $line_elements[21];
	$predicate = $line_elements[22];
	$neg = $line_elements[23];
	$pred_start_ind = $line_elements[24];
	$pred_end_ind = $line_elements[25];

	# take care of INFER and SPEC relations
	if ($predicate =~ /\(/) {
	  $predicate =~ s/\(.+\)//g;
	}

	if ($neg eq "negation") {
	  $predicate = "NEG_" . $predicate;
	}

	# read object
	$obj_maxdist = $line_elements[26];
	$obj_dist = $line_elements[27];
	$obj_cui = $line_elements[28];
	$obj_meta_name = $line_elements[29];
	@obj_semtypes = split(/,/, $line_elements[30]);
	$obj_semtype = $line_elements[31];
	$obj_entrez_id = $line_elements[32];
	$obj_entrez_name = $line_elements[33];
	$obj_text = $line_elements[34];
	$obj_score = $line_elements[38];
	$obj_start_ind = $line_elements[39];
	$obj_end_ind = $line_elements[40];

	# extract id for the object
	@obj_uis = &ReadArgumentUIs($obj_meta_name,
				    $obj_cui,
				    $obj_semtype,
				    $obj_entrez_id,
				    $obj_entrez_name);

	# take care of "None" arguments which should not
	# occur anymore.
        if ((scalar @obj_uis) == 0) {next;}

	# print out what will be inserted to the db
	# foreach (@subj_uis) {
	#  print "Subj UI:$_\n";
	# }
	# print "Subj: $subj_maxdist | $subj_dist | $subj_start_ind | $subj_end_ind | $subj_score | $subj_text\n";

	# print "Pred: $predicate | $indicator_type | $pred_start_ind | $pred_end_ind\n";

	# foreach (@obj_uis) {
	#   print "Obj UI:$_\n";
	# }
	# print "Obj: $obj_maxdist | $obj_dist | $obj_start_ind | $obj_end_ind | $obj_score | $obj_text\n";

	# each argument is a cui-semtype pair. Now we know the cui,
	# we need to find out the db id for the cui-semtype pair.
	&GetConceptSemtypeIDs(\@subj_uis, \@subj_semtypes, $subj_semtype);
	&GetConceptSemtypeIDs(\@obj_uis, \@obj_semtypes, $obj_semtype);

	# to speed it up a little bit, keep the previously seen predications in a cache.
	# So that, it is searched for every time.
	$key = join("|", sort @subj_uis) . "|" . $subj_semtype . "|" . $predicate . "|". join("|", sort @obj_uis) . "|" . $obj_semtype;
	if (exists $seen_predids{$key}) {
	  $pred_id = $seen_predids{$key};
	  # print "Pred id $pred_id is retrieved from cache.\n";
	# Otherwise, get the predication id for the given subject-predicate-object triple.
	} else {
	  $pred_id = &GetPredID(\@subj_uis, \@obj_uis, $predicate);

	  # insert predication
	  if ($pred_id eq "") {
	    $insert_predication_sth->execute($predicate,$type) or die "Couldn't execute statement: " + $insert_predication_sth->errstr;
	    $pred_id = $dbh->{q{mysql_insertid}};
	    $seen_predids{$key} = $pred_id;
	    # insert predication arguments
	    # print "Predication id: $pred_id\n";
	    # first subjects
	    foreach $ui (@subj_uis) {
	      $subj_concsem_id = $seen_concs{$ui}{$subj_semtype};
	      # print "Insert subj ui: $ui | $subj_semtype | $subj_concsem_id\n";
	      unless ($subj_concsem_id eq "") {
		$insert_predication_argument_sth->execute($pred_id,$subj_concsem_id,'S') or die "Couldn't execute statement: " + $insert_predication_argument_sth->errstr;
	      } else {
		print "Cannot insert object predication argument.\n";
	      }
	    }

	    # then objects
	    foreach $ui (@obj_uis) {
	      $obj_concsem_id = $seen_concs{$ui}{$obj_semtype};
	      # print "Insert obj ui: $ui | $obj_semtype | $obj_concsem_id\n";
	      unless ($obj_concsem_id eq "") {
		$insert_predication_argument_sth->execute($pred_id,$obj_concsem_id,'O') or die "Couldn't execute statement: " + $insert_predication_argument_sth->errstr;
	      } else {
		print "Cannot insert object predication argument.\n";
	      }
	    }
	  }
	}

	# the same predication can occur multiple times in the same sentence.
	# make sure that information is recorded in the predication_number field
	# in SENTENCE_PREDICATION table.
	# seen_predications is a hash that records the previosuly seen predications
	# for a sentence.
	unless ($pred_id == -1) {
	  if (exists $seen_predications{$sentence_id}{$pred_id}) {
	    $pred_number = $seen_predications{$sentence_id}{$pred_id} + 1;
	  } else {
	    $pred_number = 1;
	  }

	  $seen_predications{$sentence_id}{$pred_id} = $pred_number;
	  # print "Pred number: $pred_number\n";

	  # insert the sentence-predication
	  $insert_sentence_predication_sth->execute($sentence_id,$pred_id,$pred_number,$subj_dist, $subj_maxdist, $subj_start_ind, $subj_end_ind, $subj_text, $subj_score, $indicator_type, $pred_start_ind, $pred_end_ind, $obj_dist, $obj_maxdist, $obj_start_ind, $obj_end_ind, $obj_text, $obj_score) or die "Couldn't execute statement: " + $insert_sentence_predication_sth->errstr;
	}
	$prev_sentence_id = $sentence_id;
	$prev_pmid = $pmid;
	$prev_type = $senttype;
	$prev_number = $number;
      }
    }
}
close (F);

  print "Semrepping is done!\n\n";



# an argument can be from UMLS, from EntrezGene or clinical trials
# intervention list.
# Some semtypes may not be natural. (For instance, "aapp" semtype
# gets "gngm" automatically.)
# Read the argument information needed from all this weirdness
sub ReadArgumentUIs {
  my ($meta_name, $cui, $semtype, $entrez_id, $entrez_name) = @_;
  @uis = ();
  # do we really need to check for gngm and aapp?
  if ($semtype eq "gngm" || $semtype eq "aapp") {
    if ($entrez_id eq "") {
      if ($entrez_name eq "None" && $meta_name eq "") {
	push @uis, "0";
      }
    } else {
      # entrezgene ids may be multiple.
      @entrezgene_ids = split(/,/, $entrez_id);
      push @uis, @entrezgene_ids;
    }
  }
  unless ($cui eq "" || $cui eq "C0000000") {
    @cuis = split(/,/, $cui);
    push @uis, @cuis;
  }
  else {
    unless ($meta_name eq "") {
      if (exists $seen_interventions{$meta_name}) {
	$cui = $seen_interventions{$meta_name};
	push @uis, $cui;
      } else {
	# special case, this will rarely happen
	# if the concept is from ctrials intervention list, no UMLS concept can match.
	# we have to use meta_name in this case.
	$select_concept_ct_sth->execute($meta_name) or die "Couldn't execute statement: " + $select_concept_ct_sth->errstr;
	# print "Searching for intervention: $meta_name\n";
	while ($cui = $select_concept_ct_sth->fetchrow_array()) {
	  if ($cui =~ /^I/) {
	    push @uis, $cui;
	    $seen_interventions{$meta_name} = $cui;
	    # print "Found intervention: $meta_name | $cui\n";
	    last;
	  }
	}
      }
    }
  }
  return @uis;
}

# Get the concept semtype od for the given argument.
sub GetConceptSemtypeIDs {
  my($uilist, $semtype_list, $semtype) = @_;
  @uis = @{$uilist};
  @semtypes = @{$semtype_list};

  # put the concept_semtype_ids in hashes for later use
  foreach $ui (@uis) {
    unless (exists $seen_concs{$ui}{$semtype}) {
      $select_concept_sth->execute($ui, $semtype) or die "Couldn't execute statement: " + $select_concept_sth->errstr;
      $concsem_id = $select_concept_sth->fetchrow_array();
      # concept_semtype is not in db
      # probably, a synthetic semtype is being used
      if ($concsem_id eq "") {
	if ($semtype eq "gngm" || $semtype eq "aapp") {
	  foreach $sem (@semtypes) {
	    unless ($sem eq $semtype) {
	      $select_concept_sth->execute($ui,$sem) or die "Couldn't execute statement: " + $select_concept_sth->errstr;
	      $concsem_id = $select_concept_sth->fetchrow_array();
	      unless ($concsem_id eq "") { last;}
	    }
	  }
	} elsif ($semtype eq "humn") {
	  $select_humn_sth->execute($ui) or die "Couldn't execute statement: " + $select_human_sth->errstr;
	  $concsem_id = $select_humn_sth->fetchrow_array();
	}
      }
      unless ($concsem_id eq "") {
	$seen_concs{$ui}{$semtype} = $concsem_id;
      }
    }
  }

}

# having the subject and object ids and the predicate,
# find the predication id in the db
# this is complicated by the fact that an argument
# may have more than one concept.
# so the idea is to find the predication ids associated
# with each and intersect them.
# Only one predication_id must match at the end.
# The result is added to a hash for later retrieval.
sub GetPredID {
  my ($subj_ui_list, $obj_ui_list, $predicate) = @_;
  my (@subj_uis) = @{$subj_ui_list};
  my (@obj_uis) = @{$obj_ui_list};

  $i=0;
  $argcount = scalar(@subj_uis) + scalar(@obj_uis);
  $predid_found = 0;
  undef $concept_id;

  $concept_semtype_id = $seen_concs{$subj_uis[0]}{$subj_semtype};
  # print "Concept SemType Id: $concept_semtype_id \n";
  if ($concept_semtype_id eq "") { return -1;}
  $query_str = "SELECT P.PREDICATION_ID FROM PREDICATION P, PREDICATION_ARGUMENT PA, CONCEPT_SEMTYPE CS WHERE P.PREDICATE=\'" . $predicate . "\' AND P.TYPE=\'" . $type . "\' AND CS.CONCEPT_SEMTYPE_ID=\'" . $concept_semtype_id . "\' AND PA.TYPE='S' AND P.PREDICATION_ID=PA.PREDICATION_ID AND PA.CONCEPT_SEMTYPE_ID=CS.CONCEPT_SEMTYPE_ID";

  for ($i=1; $i< (scalar @subj_uis); $i++) {
    $concept_semtype_id = $seen_concs{$subj_uis[$i]}{$subj_semtype};
  # print "Concept SemType Id: $concept_semtype_id\n";
    if ($concept_semtype_id eq "") { return -1;}
    $query_str .=  " AND PA.PREDICATION_ID IN (SELECT DISTINCT PA.PREDICATION_ID FROM PREDICATION_ARGUMENT PA, CONCEPT_SEMTYPE CS WHERE CS.CONCEPT_SEMTYPE_ID=\'" . $concept_semtype_id . "\' AND PA.TYPE='S' AND PA.CONCEPT_SEMTYPE_ID=CS.CONCEPT_SEMTYPE_ID) ";
  }
  for ($i=0; $i< (scalar @obj_uis); $i++) {
    $concept_semtype_id = $seen_concs{$obj_uis[$i]}{$obj_semtype};
    if ($concept_semtype_id eq "") { return -1;}
    # print "Concept SemType Id: $concept_semtype_id\n";
    $query_str .= " AND PA.PREDICATION_ID IN (SELECT DISTINCT PA.PREDICATION_ID FROM PREDICATION_ARGUMENT PA, CONCEPT_SEMTYPE CS WHERE CS.CONCEPT_SEMTYPE_ID=\'" . $concept_semtype_id . "\' AND PA.TYPE='O' AND PA.CONCEPT_SEMTYPE_ID=CS.CONCEPT_SEMTYPE_ID)";
  }
#  $query_str .= " GROUP BY PA.PREDICATION_ID";
  # print "Predication Query: $query_str\n";
  $select_predid_sth = $dbh->prepare($query_str) or die "Couldn't prepare statement: " . $dbh->errstr;
  $select_predid_sth->execute() or die "Couldn't execute statement: " . $dbh->errstr;
#  $pred_id = $select_predid_sth->fetchrow_array();
  while ($pred_id = $select_predid_sth->fetchrow_array()) {
    $select_pa_count_sth->execute($pred_id) or die "Couldn't execute statement: " . $select_pa_count_sth->errstr;
    $predid_cnt = $select_pa_count_sth->fetchrow_array();
    # print "Argument counts: $pred_id | $predid_cnt | $argcount\n";
    if ($predid_cnt  == $argcount) {
      $predid_found = 1;
      last;
    }
  }
  if ($predid_found == 1) {
    $key = join("|", sort @subj_uis) . "|" . $subj_semtype . "|" . $predicate . "|". join("|", sort @obj_uis) . "|" . $obj_semtype;
    $seen_predids{$key} = $pred_id;
    # print "Predication Id: $pred_id\n";
    return $pred_id;
  }


  return "";

}