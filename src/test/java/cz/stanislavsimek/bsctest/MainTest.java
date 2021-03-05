package cz.stanislavsimek.bsctest;

import cz.stanislavsimek.bsctest.utils.Program;

import org.junit.Test;
import static org.junit.Assert.*;

public class MainTest {

    private final String resourcesPath = System.getProperty("testResourcesPath");
    private final String initialPackages = resourcesPath + "/initialPackages.txt";
    private final String initialFees = resourcesPath + "/initialFees.txt";
    private final String invalidFee = resourcesPath + "/invalidFee.txt";
    private final static String NL = System.lineSeparator();

    @Test
    public void quitCommandShouldEndTheProgram() throws Exception {
        Program program = new Program();
        program.setInput("quit");
        Thread thread = program.start();
        assertEquals(false, thread.isAlive());  // check program finished
        program.stop();
    }

    @Test
    public void exitCommandShouldntEndTheProgram() throws Exception {
        Program program = new Program();
        program.setInput("exit");
        Thread thread = program.start();
        assertEquals(true, thread.isAlive());  // check program still running
        assertEquals(
                "ERROR: 2 input parameters expected per package line, get 1." + NL,
                program.getErr()
        );
        program.stop();
    }

    @Test
    public void blankCommandShouldntOutputAnything() throws Exception {
        Program program = new Program();
        program.setInput(" ");
        program.start();
        assertEquals("", program.getErr());
        assertEquals(""
                + "Program started" + NL
                + "Packages:" + NL
                + "none" + NL
                + NL,
                program.getOut()
        );
        program.stop();
    }

    @Test
    public void ifNoDataLoadedGetOutputShouldReturnEmptyString() throws Exception {
        Program program = new Program();
        program.start();
        assertEquals("", program.getData());
        program.stop();
    }

    @Test
    public void ifInitialPackagesLoadedGetOutputShouldReturnExpectedData() throws Exception {
        Program program = new Program(initialPackages);
        program.start();
        assertEquals(""
                + "08801 15.960" + NL
                + "08079 5.500" + NL
                + "09300 3.200" + NL
                + "90005 2.000",
                program.getData()
        );
        program.stop();
    }

    @Test
    public void ifInitialFeesLoadedGetOutputShouldReturnExpectedData() throws Exception {
        Program program = new Program(initialPackages, initialFees);
        program.start();
        assertEquals(""
                + "08801 15.960 7.00" + NL
                + "08079 5.500 2.50" + NL
                + "09300 3.200 2.00" + NL
                + "90005 2.000 1.50",
                program.getData()
        );
        program.stop();
    }

    @Test
    public void startProgramWithBadFirstParameterShouldShowError() throws Exception {
        Program program = new Program("notExistingInitialPackages.txt");
        program.start();
        assertEquals(
                "ERROR: Unable to load initial packages from file notExistingInitialPackages.txt"
                + " : File 'notExistingInitialPackages.txt' does not exist" + NL,
                program.getErr()
        );
        program.stop();
    }

    @Test
    public void startProgramWithBadSecondParameterShouldShowError() throws Exception {
        Program program = new Program(initialPackages, "notExistingInitialFees.txt");
        program.start();
        assertEquals(
                "ERROR: Unable to load initial fees from file notExistingInitialFees.txt"
                + " : File 'notExistingInitialFees.txt' does not exist" + NL,
                program.getErr()
        );
        program.stop();
    }

    @Test
    public void badNumberFormatInInputShouldShowError() throws Exception {
        Program program = new Program();
        program.setInput("3,2 09300");
        program.start();
        assertEquals(
                "ERROR: First input parameter for package line should be float, is '3,2'." + NL,
                program.getErr()
        );
        program.stop();
    }

    @Test
    public void badNumberPrecisionInInputShouldShowError() throws Exception {
        Program program = new Program();
        program.setInput("3.2222 09300");
        program.start();
        assertEquals(
                "ERROR: Package 3.2222 kg => 09300 is invalid: net.sf.oval.ConstraintViolation:"
                + " cz.stanislavsimek.bsctest.model.Package.weightInKg must have 0 to 2147483647"
                + " integral and 0 to 3 fractional digits" + NL,
                program.getErr()
        );
        program.stop();
    }

    @Test
    public void badPostalCodeInInputShouldShowError() throws Exception {
        Program program = new Program();
        program.setInput("3.2 0930A");
        program.start();
        assertEquals(
                "ERROR: Package 3.2 kg => 0930A is invalid: net.sf.oval.ConstraintViolation:"
                + " five digits expected in postal code" + NL,
                program.getErr()
        );
        program.stop();
    }

    @Test
    public void startProgramWithInvalidFeeFileShouldShowError() throws Exception {
        Program program = new Program(initialPackages, invalidFee);
        program.start();
        assertEquals(
                "ERROR: 2 input parameters expected per fee line, get 3." + NL,
                program.getErr()
        );
        assertEquals(""
                + "08801 15.960" + NL
                + "08079 5.500" + NL
                + "09300 3.200" + NL
                + "90005 2.000",
                program.getData()
        );
        program.stop();
    }

    @Test
    public void addingAnotherPackageForExistingPostalCodeShouldBeReturnedByGetOutput() throws Exception {
        Program program = new Program(initialPackages);
        program.setInput("1 08801");
        program.start();
        assertEquals(""
                + "08801 16.960" + NL
                + "08079 5.500" + NL
                + "09300 3.200" + NL
                + "90005 2.000",
                program.getData()
        );
        program.stop();
    }

    @Test
    public void addingAnotherPackageForNewPostalCodeShouldBeReturnedByGetOutput() throws Exception {
        Program program = new Program(initialPackages);
        program.setInput("20 78991");
        program.start();
        assertEquals(""
                + "78991 20.000" + NL
                + "08801 15.960" + NL
                + "08079 5.500" + NL
                + "09300 3.200" + NL
                + "90005 2.000",
                program.getData()
        );
        program.stop();
    }

    @Test
    public void addingAnother2PackagesShouldBeReturnedByGetOutput() throws Exception {
        Program program = new Program(initialPackages);
        program.setInput(""
                + "20 78991" + NL
                + "1 08801"
        );
        program.start();
        assertEquals(""
                + "78991 20.000" + NL
                + "08801 16.960" + NL
                + "08079 5.500" + NL
                + "09300 3.200" + NL
                + "90005 2.000",
                program.getData()
        );
        program.stop();
    }

}
