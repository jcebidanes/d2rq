package de.fuberlin.wiwiss.d2rq.algebra;

import java.util.Set;

import de.fuberlin.wiwiss.d2rq.expr.Expression;
import de.fuberlin.wiwiss.d2rq.sql.ConnectedDB;

public class RelationImpl extends Relation {
	private final ConnectedDB database;
	private final AliasMap aliases;
	private final Expression condition;
	private final Set joinConditions;
	private final Set projections;
	private final boolean isUnique;
	
	public RelationImpl(ConnectedDB database, AliasMap aliases,
			Expression condition, Set joinConditions, Set projections,
			boolean isUnique) {
		this.database = database;
		this.aliases = aliases;
		this.condition = condition;
		this.joinConditions = joinConditions;
		this.projections = projections;
		this.isUnique = isUnique;
	}

	public ConnectedDB database() {
		return this.database;
	}
	
	public AliasMap aliases() {
		return this.aliases;
	}

	public Expression condition() {
		return this.condition;
	}

	public Set joinConditions() {
		return this.joinConditions;
	}

	public Set projections() {
		return projections;
	}

	public boolean isUnique() {
		return isUnique;
	}
	
	public Relation select(Expression selectCondition) {
		if (selectCondition.isTrue()) {
			return this;
		}
		if (selectCondition.isFalse()) {
			return Relation.EMPTY;
		}
		return new RelationImpl(database, aliases, condition.and(selectCondition),
				joinConditions, projections, isUnique);
	}
	
	public Relation renameColumns(ColumnRenamer renames) {
		return new RelationImpl(database, renames.applyTo(aliases),
				renames.applyTo(condition), renames.applyToJoinSet(joinConditions),
				renames.applyToProjectionSet(projections), isUnique);
	}
}