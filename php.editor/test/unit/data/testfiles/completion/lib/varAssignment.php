<?php
class clsAVarAssignment {
    /**
     * @return clsAVarAssignment
     */
    function aCreateA() {
        return new clsAVarAssignment();
    }
    /**
     * @return clsCVarAssignment
     */
    function aCreateC() {
        return new clsCVarAssignment();
    }

    /**
     * @return clsAVarAssignment
     */
    public static function aStaticCreateA() {
        return new clsAVarAssignment();
    }
    /**
     * @return clsCVarAssignment
     */
    public static function aStaticCreateC() {
        return new clsCVarAssignment();
    }

}
class clsCVarAssignment {
    /**
     * @return clsAVarAssignment
     */
    function cCreateA() {
        return new clsAVarAssignment();
    }
    /**
     * @return clsCVarAssignment
     */
    function ccreateC() {
        return new clsCVarAssignment();
    }

    /**
     * @return clsAVarAssignment
     */
    public static function cStaticCreateA() {
        return new clsAVarAssignment();
    }
    /**
     * @return clsCVarAssignment
     */
    public static function cStaticCreateC() {
        return new clsCVarAssignment();
    }

}


/**
 * @return clsAVarAssignment
 */
function fncCreateA() {
    return new clsAVarAssignment();
}
/**
 * @return clsCVarAssignment
 */
function fncCreateC() {
    return new clsAVarAssignment();
}

function mytest() {
    if (1) {
        $clsVarA=new clsAVarAssignment;
        $clsVarC=new clsCVarAssignment;
        $clsVarA1=$clsVarA->aCreateA();//test 1
        $clsVarC1=$clsVarC->ccreateC();//test 2
        $clsVarA1->aCreateA();//test 3
        $clsVarC1->cCreateA();//test 4
        $clsVarA2=$clsVarA1->aCreateA()->aCreateC()->cCreateA();
        $clsVarA2->aCreateA();//test 5
        $clsVarC2=$clsVarC1->cCreateA()->aCreateC()->cCreateA()->aCreateC();
        $clsVarC2->cCreateA();//test 6

        $stVarAA=clsAVarAssignment::aStaticCreateA();
        $stVarAC=clsAVarAssignment::aStaticCreateC();
        $stVarCA=clsCVarAssignment::cStaticCreateA();
        $stVarCC=clsCVarAssignment::cStaticCreateC();


        $stVarAA1=$stVarAA->aCreateA();//test 7
        $stVarAC1 = $stVarAC->cCreateA();//test 8
        $stVarAA1->aCreateA();//test 9
        $stVarAC1->aCreateA();//test 10
        $stVarAA2 = $stVarAC1->aCreateC()->cCreateA()->aCreateC();
        $stVarAA2->cCreateA();//test 11

        $fncVarA=fncCreateA();
        $fncVarC=fncCreateC();

        $fncVarA1=$fncVarA->aCreateA();//test 12
        $fncVarC1=$fncVarC->ccreateC();//test 13
        $fncVarA1->aCreateA();//test 14
        $fncVarC1->ccreateC();//test 15
        $fncVarA2 = $fncVarC1->cCreateA()->aCreateC()->cCreateA();
        $fncVarA2->aCreateA();//test 16
    }
    $clsVarCErr = $clsVarC;
    $clsVarCErr->//test 17
}
?>
