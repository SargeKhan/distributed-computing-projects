import java.io.IOException;

public class FSClient {

    public static void main(String args[]) {
        String configFileName = "/home/bhaiya/Documents/Assignments/DC/Client/src/config.txt";
        String filename = "file.txt";
        RemoteFile remoteFile = null;
        try {
            ReplicatedFS replFS = null;
            replFS = new ReplicatedFS(configFileName, 0);
            remoteFile = replFS.open(filename);

        } catch (IOException e) {
          e.printStackTrace();
        }

        int byteOffset = 0;
        //Write incrementing numbers to the file.
        for (int loopCnt = 0; loopCnt < 128; loopCnt++) {
            String strData = new Integer(loopCnt).toString() + "\n";

            if ( remoteFile.writeBlock(strData.getBytes(), byteOffset) < 0 ) {
                System.out.println("Error writing to file " + remoteFile.getName() + " [LoopCnt=" + loopCnt + "]\n" );
                System.exit(-1);
            }

            byteOffset += strData.length();
        }

        //Actually store the file on all of the remote servers
        if (remoteFile.commit() < 0) {
            System.err.println( "Could not commit changes to file: " + remoteFile.getName() );
            System.exit(-1);
        }

        //Close the file: release resources and commit any changes.
        if (remoteFile.close() < 0) {
            System.err.println("Could not close file: " + remoteFile.getName());
            System.exit(-1);
        }

    }

}
