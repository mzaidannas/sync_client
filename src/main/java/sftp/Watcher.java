package sftp;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import sync_clients.SyncClient;

import static java.nio.file.StandardWatchEventKinds.*;

public class Watcher {
    WatchService watcher;
    SyncClient syncClient;
    Logger logger;

    Watcher(SyncClient syncClient) {
        this.syncClient = syncClient;

        // Setup logger
        logger = Logger.getLogger(Watcher.class.getName());
        logger.addHandler(new ConsoleHandler());
        try {
            logger.addHandler(new FileHandler());
        } catch (IOException ex) {
            logger.warning("Error creating a log file");
        }

        // Setup watching service
        try {
            logger.info("Watcher service creating");
            watcher = FileSystems.getDefault().newWatchService();

        } catch (IOException ex) {
            logger.severe("Exception while creating watcher service");
        }
    }

    Watcher(SyncClient syncClient, Logger logger) {
        this.syncClient = syncClient;
        this.logger = logger;

        // Setup watching service
        try {
            logger.info("Watcher service creating");
            watcher = FileSystems.getDefault().newWatchService();

        } catch (IOException ex) {
            logger.severe("Exception while creating watcher service");
        }
    }

    public void watch(String path, String remoteDir) {
        Path dir = FileSystems.getDefault().getPath(path);
        try {
            WatchKey key = dir.register(watcher,
                                ENTRY_CREATE,
                                ENTRY_DELETE,
                                ENTRY_MODIFY);
        } catch (IOException x) {
            System.err.println(x);
        }

        for (;;) {

            // wait for key to be signaled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                // This key is registered only
                // for ENTRY_CREATE events,
                // but an OVERFLOW event can
                // occur regardless if events
                // are lost or discarded.
                if (kind == OVERFLOW) {
                    continue;
                }

                // The filename is the
                // context of the event.
                WatchEvent<Path> ev = (WatchEvent<Path>)event;
                Path filename = ev.context();

                // Verify that the new
                //  file is a text file.
                try {
                    // Resolve the filename against the directory.
                    // If the filename is "test" and the directory is "foo",
                    // the resolved name is "test/foo".
                    Path child = dir.resolve(filename);
                    // if (!Files.probeContentType(child).equals("text/plain")) {
                    //     System.err.format("New file '%s'" +
                    //         " is not correctly formated file.%n", filename);
                    //     continue;
                    // }
                    // else {
                        if (kind == ENTRY_DELETE) {
                            logger.info(String.format("Deleting file %s from FTP Server%n", filename));
                            syncClient.delete(child.toString(), remoteDir);
                        } else {
                            logger.info(String.format("Sending file %s to FTP Server%n", filename));
                            syncClient.send(child.toString(), remoteDir);
                        }
                    // }
                } catch (Exception x) {
                    System.err.println(x);
                    continue;
                }
            }

            // Reset the key -- this step is critical if you want to
            // receive further watch events.  If the key is no longer valid,
            // the directory is inaccessible so exit the loop.
            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }
}
