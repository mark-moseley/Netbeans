<?
// If done processing a 'param', append it to the 'params' array. -> manually added
if (!strcmp($name, 'param')) {
    $this->record['params'][] = array_pop($this->stack);
}
// If done with the 'record' parsing, dump it. -> manually added
    elseif (!strcmp($name, 'record')) {
    // TODO: replace with DB INSERT --> manually added
    //    print_r($this->record); --> ctrl+/
        $this->normalizeRecord();
        //comment(); --> manually added
        $this->storeRecord();
        array_pop($this->stack);
    }
?>