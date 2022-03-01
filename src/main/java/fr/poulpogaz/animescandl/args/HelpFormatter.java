package fr.poulpogaz.animescandl.args;

import fr.poulpogaz.animescandl.utils.Utils;

import java.util.Comparator;
import java.util.List;

public class HelpFormatter {

    /** Max number of characters for each help string per line  */
    private static final int MAX_OPT_HELP_WIDTH = 74;

    private static final String DEFAULT_ARG_NAME = "ARG";

    private static final Comparator<Option> optComparator = Utils.comparing(Option::getName);
    private static final Comparator<OptionGroup> groupComparator = Utils.comparing(OptionGroup::getName);

    public static void printHelp(String usage, Options options) {
        System.out.println(helpString(usage, options));
    }

    public static String helpString(String usage, Options options) {
        StringBuilder builder = new StringBuilder();
        builder.append("Usage: ").append(usage).append("\n\n");

        OptionGroup unnamed = options.getGroup(null);
        if (unnamed != null) {
            printGroup(builder, unnamed);
        }

        List<OptionGroup> groups = options.getOptions().values().stream().sorted(groupComparator).toList();

        for (OptionGroup g : groups) {
            if (g != unnamed) {
                printGroup(builder, g);
            }
        }

        return builder.toString();
    }

    private static void printGroup(StringBuilder builder, OptionGroup group) {
        String indent = "";
        if (group.getName() != null) {
            builder.append(group.getName()).append(":\n");
            indent = " ";
        }

        int width = getWidth(group);
        String descIndent = indent + " ".repeat(10 + width);

        List<Option> options = group.getOptions().stream().sorted(optComparator).toList();
        for (Option option : options) {
            builder.append(indent);

            String sn = option.getShortName();
            String n = option.getName();
            String arg = option.getArgumentName();

            // short name and long name
            int pos = builder.length();
            if (sn != null) {
                builder.append("-").append(sn);

                if (n != null) {
                    builder.append(", --").append(n);
                }
            } else {
                builder.append("--").append(n);
            }

            // arg
            if (option.hasArgument()) {
                builder.append(" <")
                        .append(arg == null ? DEFAULT_ARG_NAME : arg)
                        .append(">");
            }

            // space
            int pos2 = builder.length();
            builder.append(" ".repeat(10 + width - (pos2 - pos)));

            // description
            String desc = option.getDescription();
            if (desc != null) {
                appendTextBlock(builder, desc, descIndent, MAX_OPT_HELP_WIDTH);
            }

            builder.append('\n');
        }
        builder.append('\n');
    }

    private static void appendTextBlock(StringBuilder builder, String text, String indent, int width) {
        int index = 0;
        int x = 0;
        while (index < text.length()) {
            int l = wordLength(text, index);

            if (l + x < width) {
                builder.append(text, index, index + l).append(" ");
                x += l + 1;
                index += l + 1;
            } else if (x == 0) { // the whole word doesn't fit
                l = width - 1;
                builder.append(text, index, index + l).append("-");
                builder.append("\n").append(indent);
                index += l + 1;
            } else {
                x = 0;
                builder.append("\n").append(indent);
            }
        }
    }

    private static int wordLength(String text, int start) {
        int length = 0;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);

            if (Character.isWhitespace(c)) {
                break;
            }
            length++;
        }

        return length;
    }

    private static int getWidth(OptionGroup group) {
        int width = 0;

        for (Option option : group.getOptions()) {
            String sn = option.getShortName();
            String n = option.getName();
            String argName = option.getArgumentName();

            int w = 0;
            if (sn != null) {
                w += sn.length() + 1; // -

                if (n != null) {
                    w += n.length() + 4; // comma + space + --
                }
            } else {
                w += n.length() + 2; // --
            }

            if (option.hasArgument()) {
                if (option.getArgumentName() != null) {
                    w += argName.length() + 2; // < and >
                } else {
                    w += DEFAULT_ARG_NAME.length() + 2; // < and >
                }
            }

            width = Math.max(width, w);
        }

        return width;
    }
}
