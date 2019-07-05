#!/usr/bin/perl
#
# Updates the scc1 table to represent an updated, summarized version of the tables SENTENCE, SENTENCE_PREDICATION, PREDICATION, etc.
# After the execution of this script, scc1 contains the join of all those tables.
# The script is necesary since the current configuration of mysqld does not allow the
# huge query to run to completion.

use DBI;

$dbname = shift @ARGV;
chomp $dbname;

$dbh = DBI->connect("dbi:mysql:database=" . $dbname . ";host=indsrv2:3306;user=root;password=indsrv1\@root") or die "Couldn't connect to database: " . DBI->errstr;
$delete_scc1_sth = $dbh->prepare_cached("DELETE FROM PREDICATE_AGGREGATE") or die "Couldn't prepare statement: " . $dbh->errstr;
$insert_scc1_sth = $dbh->prepare_cached('insert ignore into PREDICATE_AGGREGATE (PID,SID,PNUMBER, PMID,predicate,s_cui,s_name,s_type,s_novel,o_cui,o_name,o_type,o_novel)
  select P.PREDICATION_ID, S.SENTENCE_ID, SP.PREDICATION_NUMBER, S.PMID, P.PREDICATE, group_concat(SC.CUI separator \'|||\'),
  group_concat(SC.PREFERRED_NAME separator \'|||\'),group_concat(SS.SEMTYPE separator \'|||\'),max(SS.NOVEL)=\'Y\',group_concat(OC.CUI separator \'|||\'),
  group_concat(OC.PREFERRED_NAME separator \'|||\'),group_concat(OS.SEMTYPE separator \'|||\'),max(OS.NOVEL)=\'Y\' from  (select * from SENTENCE) S,
  SENTENCE_PREDICATION SP, PREDICATION P, CONCEPT SC, CONCEPT_SEMTYPE SS, PREDICATION_ARGUMENT SPA, CONCEPT OC, CONCEPT_SEMTYPE OS, PREDICATION_ARGUMENT OPA
  WHERE
  S.SENTENCE_ID = SP.SENTENCE_ID and
  SP.PREDICATION_ID = P.PREDICATION_ID and
  SPA.PREDICATION_ID = P.PREDICATION_ID and
  SPA.TYPE=\'S\' AND
  SS.CONCEPT_SEMTYPE_ID = SPA.CONCEPT_SEMTYPE_ID AND
  SC.CONCEPT_ID = SS.CONCEPT_ID AND
  OPA.PREDICATION_ID = P.PREDICATION_ID and
  OPA.TYPE=\'O\' AND
  OS.CONCEPT_SEMTYPE_ID = OPA.CONCEPT_SEMTYPE_ID
  AND OC.CONCEPT_ID = OS.CONCEPT_ID
  group by SP.PREDICATION_ID, SP.SENTENCE_ID, SP.PREDICATION_NUMBER') or die "Couldn't prepare statement: " . $dbh->errstr;

$dbh->do('SET NAMES utf8');
   # $delete_scc1_sth->execute() or die "Couldn't execute statement: " + $insert_scc1_sth->errstr;

  $insert_scc1_sth->execute() or die "Couldn't execute statement: " + $insert_scc1_sth->errstr;
  print "All citations loaded into database.\n\n";


