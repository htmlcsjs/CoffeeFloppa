package net.htmlcsjs.coffeeFloppa.commands;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StoikCommandTest {

    @Test
    void parseChemical() {
        // caseOne - C6H12O6 - simple molecule
        List<String> caseOneOutput = StoikCommand.parseChemical("C6H12O6");
        List<String> caseOneExpected = Arrays.asList("CCCCCCHHHHHHHHHHHHOOOOOO".split(""));
        assertTrue(caseOneExpected.containsAll(caseOneOutput) && caseOneExpected.size() == caseOneOutput.size(), "caseOne - C6H12O6 - simple molecule");

        // caseTwo - 2CH3COOH - more than one mole of a molecule
        List<String> caseTwoOutput = StoikCommand.parseChemical("2CH3COOH");
        List<String> caseTwoExpected = Arrays.asList("CHHHCOOHCHHHCOOH".split(""));
        assertTrue(caseTwoExpected.containsAll(caseTwoOutput) && caseTwoExpected.size() == caseTwoOutput.size(), "caseTwo - 2CH3COOH - more than one mole of a molecule");

        // caseThree - Rh2(SO4)3 - chemical with sub chemicals
        List<String> caseThreeOutput = StoikCommand.parseChemical("Rh2(SO4)3");
        List<String> caseThreeExpected = new ArrayList<>(Arrays.asList("SOOOOSOOOOSOOOO".split("")));
        caseThreeExpected.add("Rh");
        caseThreeExpected.add("Rh");
        assertTrue(caseThreeExpected.containsAll(caseThreeOutput) && caseThreeExpected.size() == caseThreeOutput.size(), "caseThree - Rh2(SO4)3 - chemical with sub chemicals");

        // caseFour - (H2O)3(Rh2(SO4)3) - chemical with nested subchemicals
        List<String> caseFourOutput = StoikCommand.parseChemical("(H2O)3(Rh2(SO4)3)");
        List<String> caseFourExpected = new ArrayList<>(Arrays.asList("HHOHHOHHOSOOOOSOOOOSOOOO".split("")));
        caseFourExpected.add("Rh");
        caseFourExpected.add("Rh");
        assertTrue(caseFourExpected.containsAll(caseFourOutput) && caseFourExpected.size() == caseFourOutput.size(), "caseFour - (H2O)3(Rh2(SO4)3) - chemical with nested subchemicals");

        // caseFive - 2(NH4)2Ce(NO3)6 - more than one mole of a chemical with sub chemicals
        List<String> caseFiveOutput = StoikCommand.parseChemical("2(NH4)2Ce(NO3)6");
        List<String> caseFiveExpected = new ArrayList<>(Arrays.asList(("NHHHHNHHHHNOOONOOONOOONOOONOOONOOONHHHHNHHHHNOOONOOONOOONOOONOOONOOO").split("")));
        caseFiveExpected.add("Ce");
        caseFiveExpected.add("Ce");
        assertTrue(caseFiveExpected.containsAll(caseFiveOutput) && caseFiveExpected.size() == caseFiveOutput.size(), "caseFive - 2(NH4)2Ce(NO3)6 - more than one mole of a chemical with sub chemicals");

        // caseSix - 5(H2O)3((FeW)5CrMo2V)6CoMnSi - made a lapse in judgement I do not wish to repeat
        List<String> caseSixOutput = StoikCommand.parseChemical("5(H2O)3((FeW)5CrMo2V)6CoMnSi");
        List<String> caseSixExpected = StoikCommand.parseChemical("H30O15Co5Cr30Fe150Mn5Mo60Si5V30W150");
        assertTrue(caseSixExpected.containsAll(caseSixOutput) && caseSixExpected.size() == caseSixOutput.size(), "caseSix - 5(H2O)3((FeW)5CrMo2V)6CoMnSi - made a lapse in judgement I do not wish to repeat");
    }
}
