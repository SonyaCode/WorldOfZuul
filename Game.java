/**
 *  This class is the main class of the "World of Zuul" application. 
 *  "World of Zuul" is a very simple, text based adventure game.  Users 
 *  can walk around some scenery. That's all. It should really be extended 
 *  to make it more interesting!
 * 
 *  To play this game, create an instance of this class and call the "play"
 *  method.
 * 
 *  This main class creates and initialises all the others: it creates all
 *  rooms, creates the parser and starts the game.  It also evaluates and
 *  executes the commands that the parser returns.
 * 
 * @author  Michael Kölling and David J. Barnes
 * @version 2016.02.29
 */
public class Game 
{
    private Parser parser;
    private Room currentRoom;
    private boolean hasKey;
    private boolean foundTreasure;
        
    /**
     * Create the game and initialise its internal map.
     */
    public Game() 
    {
        createRooms();
        parser = new Parser();
        hasKey = false;
        foundTreasure = false;
    }

    /**
     * Create all the rooms and link their exits together.
     */
    private void createRooms()
    {
        Room mondstadt, village, dragonspine, liyue,
             toInazumaOcean, inazuma, theChasm, theChasmCave, sumeru,
             desert, toFontaineOcean, fontaine;
      
        // create the rooms
        mondstadt = new Room("in Mondstadt");
        village = new Room("in a Village");
        dragonspine = new Room("in Dragonspine");
        liyue = new Room("in Liyue");
        toInazumaOcean = new Room("in the ocean that's located at the South of Liyue and North of Inazuma");
        inazuma = new Room("in Inazuma");
        theChasm = new Room("in The Chasm that's located at the North of Liyue");
        theChasmCave = new Room("in the cave below The Chasm");
        sumeru = new Room("in Sumeru that's located at the West of The Chasm");
        desert = new Room("in the Desert that's located at the West of Sumeru");
        toFontaineOcean = new Room("in the ocean that's located at the North of the Desert and South of Fontaine"); 
        fontaine = new Room("in Fontaine");
        
        // initialise room exits (north, east, south, west)
        mondstadt.setExits(null, null, village, null);
        village.setExits(mondstadt, null, null, dragonspine);
        dragonspine.setExits(null, village, null, liyue);

        liyue.setExits(theChasm, dragonspine, toInazumaOcean, null);
        toInazumaOcean.setExits(liyue, null, inazuma, null);
        inazuma.setExits(toInazumaOcean, null, null, null);
        theChasm.setExits(null, null, liyue, sumeru);
        sumeru.setExits(null, theChasm, null, desert);
        desert.setExits(toFontaineOcean, sumeru, null, null);
        toFontaineOcean.setExits(fontaine, null, desert, null);
        fontaine.setExits(null, null, toFontaineOcean, null);

        // upstair and downstairs
        theChasm.setExit("down", theChasmCave);
        theChasmCave.setExit("up", theChasm);

        

        currentRoom = mondstadt;  // start game outside
    }

    /**
     *  Main play routine.  Loops until end of play.
     */
    public void play() 
    {            
        printWelcome();

        // Enter the main command loop.  Here we repeatedly read commands and
        // execute them until the game is over.
                
        boolean finished = false;
        while (! finished) {
            Command command = parser.getCommand();
            finished = processCommand(command);
        }
        System.out.println("Thank you for playing.  Good bye.");
    }

    /**
     * Print out the opening message for the player.
     */
    private void printWelcome()
    {
        System.out.println();
        System.out.println("Welcome to the World of Zuul!");
        System.out.println("World of Zuul is a new, incredibly boring adventure game.");
        System.out.println("Type 'help' if you need help.");
        System.out.println();
        printLocationInfo();
    }

    /**
     * Given a command, process (that is: execute) the command.
     * @param command The command to be processed.
     * @return true If the command ends the game, false otherwise.
     */
    private boolean processCommand(Command command) 
    {
        boolean wantToQuit = false;

        if(command.isUnknown()) {
            System.out.println("I don't know what you mean...");
            return false;
        }

        String commandWord = command.getCommandWord();
        if (commandWord.equals("help")) {
            printHelp();
        }
        else if (commandWord.equals("go")) {
            goRoom(command);
        }
        else if (commandWord.equals("quit")) {
            wantToQuit = quit(command);
        }
        else if (commandWord.equals("pick") && !(currentRoom.getDescription().equals("in the cave below The Chasm"))) {
            System.out.println("There is nothing to pick up...");
        } 
        else if (commandWord.equals("pick") && (currentRoom.getDescription().equals("in the cave below The Chasm"))) {
            foundKey(command);
        } 
        else if (commandWord.equals("unlock") && !(currentRoom.getDescription().equals("in Fontaine"))) {
            System.out.println("There is nothing to unlock...");
        } 
        else if (commandWord.equals("unlock") && (currentRoom.getDescription().equals("in Fontaine"))) {
            foundChestBox(command);
            wantToQuit = foundTreasure;
        }

        return wantToQuit;
    }

    // implementations of user commands:

    /**
     * Print out some help information.
     * Here we print some stupid, cryptic message and a list of the 
     * command words.
     */
    private void printHelp() 
    {
        System.out.println("You are an adventurer, searching for an ancient treasure that was left by Human v.s. Demon war.");
        System.out.println("Your goal is to travel to 5 nations to find the treasure.");
        System.out.println();
        System.out.println("Your command words are:");
        System.out.println("   go quit help");
    }

    /** 
     * Try to go in one direction. If there is an exit, enter
     * the new room, otherwise print an error message.
     */
    private void goRoom(Command command) 
    {
        if(!command.hasSecondWord()) {
            // if there is no second word, we don't know where to go...
            System.out.println("Go where?");
            return;
        }

        String direction = command.getSecondWord();

        // Try to leave current room.
        Room nextRoom = null;
        nextRoom = currentRoom.getExit(direction);

        if (nextRoom == null) {
            System.out.println("There is no door!");
        }
        else {
            currentRoom = nextRoom;
            printLocationInfo();
        }
    }


    // print the locations and the exits
    private void printLocationInfo() {
        System.out.println("You are " + currentRoom.getDescription());

        // if the player is at the cave below the Chasm or Fontaine, tell them about the key and chest
        if (currentRoom.getDescription().equals("in the cave below The Chasm")) {
            System.out.println("You found a mysterious key in the cave! Do you want to pick it up?");
        } else if (currentRoom.getDescription().equals("in Fontaine")) {
            System.out.println("You found a chest. There is a lock on it. Do you want to unlock it?");
        }

        System.out.print("You can go:");
        System.out.println(currentRoom.getExitString());
        System.out.println();
    }


    // allows the player to pick up the key or not
    private void foundKey(Command command) {
        if (currentRoom.getDescription().equals("in the cave below The Chasm")) {
            String pickOrNot = command.getCommandWord();

            if (pickOrNot.equalsIgnoreCase("pick")) {
                hasKey = true;
                System.out.println("You picked up the key.");
                System.out.println("Hmmm... What could it be used for?");
            } else {
                System.out.println("You left the key there.");
            }
            System.out.println();
        }
    }

    // allows the player to unlock the chest box or not
    private void foundChestBox(Command command) {
        if (currentRoom.getDescription().equals("in Fontaine")) {
            String unlockOrNot = command.getCommandWord();

            if (unlockOrNot.equalsIgnoreCase("unlock")) {
                if (hasKey) {
                    foundTreasure = true;
                    System.out.println("Woah, there are tons of jewelry and golds! You found the treasure!!!");
                    System.out.println("CONGRATS!! You won the game!");
                } else {
                    System.out.println("You don't have a key :(");
;                }
            }
        }
    }

    /** 
     * "Quit" was entered. Check the rest of the command to see
     * whether we really quit the game.
     * @return true, if this command quits the game, false otherwise.
     */
    private boolean quit(Command command) 
    {
        if(command.hasSecondWord()) {
            System.out.println("Quit what?");
            return false;
        }
        else {
            return true;  // signal that we want to quit
        }
    }
}