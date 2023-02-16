package hoi4_coding.code;

/**
 * Scopes can be divided into types by the targets of the scope for which effects are executed
 * or triggers are checked:
 * <p>
 * Country scopes - Executed for countries.<p>
 * State scopes - Executed for states.<p>
 * Character scopes - Executed for characters. Some subsets exist, such as unit leaders and country leaders.<p>
 * Division scopes - Executed for divisions.<p>
 * <a href="https://hoi4.paradoxwikis.com/Scopes">Hoi4 Wiki</a>
 */
public enum ScopeTargetType {
    COUNTRY,
    STATE,
    CHARACTER,
    DIVISION,
    ANY
}
