package net.rwchess.services;

import net.rwchess.persistent.Bucket;
import net.rwchess.persistent.TournamentPlayer;
import net.rwchess.utils.UsefulMethods;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.util.PythonInterpreter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PythonBucketsGenerationService {

    private List<Bucket> cachedBuckets;
    private int cacheHash;

    private String pythonDir;

    private static String[] td = {"BethanyGrace", "BethanyGrace", "RoyRogersC", "BethanyGrace", "PankracyRozumek", "pchesso", "RoyRogersC"};

    public PythonBucketsGenerationService(String pythonDir) {
        this.pythonDir = pythonDir;
    }

    public List<Bucket> generateBuckets(List<TournamentPlayer> players) {
        if (players.hashCode() == cacheHash)
            return cachedBuckets;

        StringBuilder inputArr = new StringBuilder("[");

        for (TournamentPlayer player : players) {
            inputArr.append('(').append(player.getFixedRating()).append(", '").append(player.getAssocMember().getUsername()).append("'),");
        }
        inputArr.append(']');

        PythonInterpreter interp =
                new PythonInterpreter();

        interp.execfile(pythonDir + "bucket_generator.py");
        interp.exec("generator = BucketGenerator(" + inputArr + ')');
        interp.exec("generator.split_players_to_buckets()");

        int bucketsCount = interp.eval("len(generator.buckets)").asInt();

        List<Bucket> buckets = new ArrayList<Bucket>();
        for (int i = 0; i < bucketsCount; i++) {
            List<TournamentPlayer> bucketArr = new ArrayList<TournamentPlayer>();

            PyList pyBucket = new PyList(interp.eval("generator.buckets[generator.bucket_names[" + i + "]]"));
            PyObject[] tupleArray = pyBucket.getArray();
            for (PyObject tuple : tupleArray) {
                PyTuple pyTuple = (PyTuple) tuple;

                try {
                    bucketArr.add(UsefulMethods.findByName(players, pyTuple.getArray()[1].toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Bucket bucket = new Bucket();
            bucket.setName(interp.eval("generator.bucket_names[" + i + ']').asString());
            bucket.setPlayerList(bucketArr);
            bucket.setTd(td[i]);
            buckets.add(bucket);

        }

        cachedBuckets = buckets;
        cacheHash = players.hashCode();
        return buckets;
    }
}
