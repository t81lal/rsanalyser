package org;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;

import org.nullbool.api.AbstractAnalysisProvider;
import org.nullbool.api.Context;
import org.nullbool.api.Revision;
import org.nullbool.api.util.RSVersionHelper;
import org.nullbool.impl.AnalysisProviderImpl;
import org.nullbool.impl.AnalysisProviderRegistry;
import org.nullbool.impl.AnalysisProviderRegistry.ProviderCreator;
import org.nullbool.impl.AnalysisProviderRegistry.RegistryEntry;
import org.nullbool.impl.r77.AnalysisProvider77Impl;
import org.nullbool.impl.r79.AnalysisProvider79Impl;
import org.nullbool.impl.r82.AnalysisProvider82Impl;
import org.nullbool.impl.r90.AnalysisProvider90Impl;
import org.topdank.banalysis.filter.Filter;

/**
 * @author Bibl (don't ban me pls)
 * @created 4 May 2015
 */
public class Boot {

    public static final File GAMEPACK_DIRECTORY = Paths.get(System.getProperty("user.home"), "Dropbox", "OSRS RE collection", "gamepacks").toFile();

    public static void main(String[] args) throws Exception {
        System.out.printf("Remote rev: %d.%n", RSVersionHelper.getVersion(RSVersionHelper.getServerAddress(58), 77, 100));
        File gamepackFile = new File(GAMEPACK_DIRECTORY, args[0] + ".jar");
        bootstrap();
        Revision revision = new Revision(args[0], gamepackFile);
        runLatest(AnalysisProviderRegistry.get(revision).create(revision));
        System.exit(1);
    }


    private static void runFlags(AbstractAnalysisProvider provider, Map<String, Boolean> flags) throws Exception {
        try {
            Context.bind(provider);
            provider.run();
        } finally {
            Context.unbind();
        }
    }



    private static void runLatest(AbstractAnalysisProvider provider) throws Exception {
        Map<String, Boolean> flags = provider.getFlags();
        flags.put("nodump", false);
        flags.put("debug", true);
        flags.put("reorderfields", true);
        flags.put("multis", true);
        flags.put("logresults", true);
        flags.put("verify", false);
        flags.put("paramdeob", true);
        // flags.put("generateheaders", true);
        runFlags(provider, flags);
    }
    private static void bootstrap() throws Exception {
        AnalysisProviderRegistry.register(new RegistryEntry(new ProviderCreator() {
            @Override
            public AbstractAnalysisProvider create(Revision rev) throws Exception {
                return new AnalysisProviderImpl(rev);
            }
        }).addFilter(new Filter<Revision>() {
            @Override
            public boolean accept(Revision t) {
                return true;
            }
        }));

		/* Adds it before the default implementation. */
        AnalysisProviderRegistry.register(new RegistryEntry(new ProviderCreator() {
            @Override
            public AbstractAnalysisProvider create(Revision rev) throws Exception {
                return new AnalysisProvider77Impl(rev);
            }
        }).addFilter(new Filter<Revision>() {
            @Override
            public boolean accept(Revision t) {
                if (t == null)
                    return false;

                try {
                    int val = Integer.parseInt(t.getName());
                    return val >= 77;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }));

        AnalysisProviderRegistry.register(new RegistryEntry(new ProviderCreator() {
            @Override
            public AbstractAnalysisProvider create(Revision rev) throws Exception {
                return new AnalysisProvider79Impl(rev);
            }
        }).addFilter(new Filter<Revision>() {
            @Override
            public boolean accept(Revision t) {
                if (t == null)
                    return false;

                try {
                    int val = Integer.parseInt(t.getName());
                    return val >= 79;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }));

        AnalysisProviderRegistry.register(new RegistryEntry(new ProviderCreator() {
            @Override
            public AbstractAnalysisProvider create(Revision rev) throws Exception {
                return new AnalysisProvider82Impl(rev);
            }
        }).addFilter(new Filter<Revision>() {
            @Override
            public boolean accept(Revision t) {
                if (t == null)
                    return false;

                try {
                    int val = Integer.parseInt(t.getName());
                    return val >= 82;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }));

        AnalysisProviderRegistry.register(new RegistryEntry(new ProviderCreator() {
            @Override
            public AbstractAnalysisProvider create(Revision rev) throws Exception {
                return new AnalysisProvider90Impl(rev);
            }
        }).addFilter(new Filter<Revision>() {
            @Override
            public boolean accept(Revision t) {
                if (t == null)
                    return false;

                try {
                    int val = Integer.parseInt(t.getName());
                    return val >= 90;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }));
    }
} 