package intellijcoder.workspace;

import intellijcoder.model.Problem;

/**
 * @author Konstantin Fadeyev
 *         21.01.11
 */
public abstract class CodeBuilder {
    private StringBuilder builder;
    private int level;

    public CodeBuilder() {
        builder = new StringBuilder();
    }

    protected void endBlock() {
        level--;
        addRow("}");
    }

    protected void startBlock(String row) {
        indent();
        builder.append(row).append(" {\n");
        level++;
    }

    protected void addRow(String row) {
        indent();
        builder.append(row).append('\n');
    }

    private void indent() {
        for(int i = 0; i < level; i++) {
            builder.append('\t');
        }
    }

    public String build(Problem problem) {
        reset();
        doBuild(problem);
        return builder.toString();
    }

    protected abstract void doBuild(Problem problem);

    private void reset() {
        builder = new StringBuilder();
    }
}
