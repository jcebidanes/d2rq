package de.fuberlin.wiwiss.d2rq.rdql;

import java.util.Collection;
import java.util.NoSuchElementException;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

import de.fuberlin.wiwiss.d2rq.D2RQException;
import de.fuberlin.wiwiss.d2rq.sql.ResultRow;
import de.fuberlin.wiwiss.d2rq.sql.TripleMaker;

/**
 * Contains the result set from one SQL query and transforms it into triples.
 * A triple is produced for TripleMaker in TripleMaker and each row in the result set.
 *
 * @author jgarbers
 * @version $Id: ApplyTripleMakerRowIterator.java,v 1.5 2006/09/09 23:25:15 cyganiak Exp $
 */
public class ApplyTripleMakerRowIterator implements ClosableIterator {
	private ClosableIterator sqlIterator;
	private TripleMaker[] tripleMakers = null;
	private Triple[] prefetchedRow;
	private boolean explicitlyClosed = false;

	public ApplyTripleMakerRowIterator(ClosableIterator sqlIterator, TripleMaker[] tripleMakers) {
		this.sqlIterator = sqlIterator;
		this.tripleMakers = tripleMakers;
	}
	
	public boolean hasNext() {
		if (this.explicitlyClosed) {
			return false;
		}
		if (this.prefetchedRow == null) {
			this.prefetchedRow = tryFetchNextRow();
		}
		return this.prefetchedRow != null;
	}

	public Triple[] nextRow() {
		if (this.prefetchedRow != null) {
			Triple[] result = this.prefetchedRow;
			this.prefetchedRow = null;
			return result;
		}
		throw new NoSuchElementException();
	}
	
	public Object next() {
		return nextRow();
	}
	
	public void close() {
		this.explicitlyClosed = true;
		this.sqlIterator.close();
	}
	
	public void remove() {
		throw new RuntimeException("Operation not supported");
	}
	
	private Triple[] tryFetchNextRow() {
		while (this.sqlIterator.hasNext()) {
			Triple[] nextRow = makeTripleRow((ResultRow) this.sqlIterator.next());
			if (nextRow != null) {
				return nextRow;
			}
		}
		this.sqlIterator.close();
		return null;
    }
	
	private Triple[] makeTripleRow(ResultRow row) {
		Triple[] result = new Triple[tripleMakers.length];
		for (int i = 0; i < tripleMakers.length; i++) {
			Collection triples = tripleMakers[i].makeTriples(row);
			if (triples.isEmpty()) {
				return null;
			}
			if (triples.size() > 1) {
				throw new D2RQException(
						"Multi-triple result not supported here. Result was: " + triples);
			}
			result[i] = (Triple) triples.iterator().next();
		}
		return result;
	}
}