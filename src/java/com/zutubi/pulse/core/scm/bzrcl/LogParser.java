package com.zutubi.pulse.core.scm.bzrcl;

import static com.zutubi.pulse.core.scm.bzrcl.BzrConstants.*;
import com.zutubi.util.logging.Logger;

import java.lang.Integer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;

import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.api.FileChange.Action;
import com.zutubi.pulse.core.util.api.XMLUtils;

/** Revisions look like
------------------------------------------------------------
revno: 1
committer: Barack Obama <prez@whitehouse.gov>
branch nick: code
timestamp: Tue 2011-09-06 15:01:35 +0100
message:
  frist!
added:
  Development/Java/HelloWorld.java
modified:
  Development/Java/Code.java
*/

public class LogParser
{
    private static final String START = "------------------------------------------------------------";
    private static final String REVISION = "revno:\\s+([0-9]+)";
    private static final String TIME = "timestamp:\\s+(.*)";
    private static final String AUTHOR = "committer:(.*)";
    private static final String COMMENT = "message:";
    private static final String ADDED = "added:";
    private static final String MODIFIED = "modified:";
    private static final String REMOVED = "removed:";
    private static final String RENAMED = "renamed:";
    private static final String MOVEDSTRING = "(.*?) => (.*)";

    private static Pattern mRevisionPattern = Pattern.compile(REVISION);
    private static Pattern mAuthorPattern = Pattern.compile(AUTHOR);
    private static Pattern mTimePattern = Pattern.compile(TIME);
    private static Pattern mMovedPattern = Pattern.compile(MOVEDSTRING);
    
    private static final Logger LOG = Logger.getLogger(LogParser.class);

    private enum ParseState
    {
        NONE,
        MESSAGE,
        ADDED,
        MODIFIED,
        RENAMED,
        REMOVED,
        MOVED
    }

    public static List<Changelist> parse(List<String> lines) throws ScmException
    {
        try
        {
            List<Changelist> result = new LinkedList<Changelist>();
            List<String> singleMessage = new LinkedList<String>();

            for( String l : lines )
            {
                if( l.compareTo(START) == 0 &&
                    singleMessage.size() > 0 )
                {
                    processEntry( singleMessage, result );
                    singleMessage.clear();
                }
                else
                    singleMessage.add(l);
            }

            // also process when we reach the end ..            
            if( singleMessage.size() > 0 )
                 processEntry( singleMessage, result );
            
            return result;
        }
        catch (ParsingException e)
        {
            throw new ScmException("Unable to parse log output: " + e.getMessage(), e);
        }
    }

    private static void processEntry ( List<String> singleMessage, List<Changelist> result) throws ParsingException
    {
        Revision revision = new Revision( 1 );
        String author = "author";
        String dateString = "date";
        String message = "";
        List<FileChange> changes = new LinkedList<FileChange>();
        ParseState state = ParseState.NONE;

        for( String line : singleMessage )
        {
            Matcher revisionMatcher = mRevisionPattern.matcher(line);
            Matcher authorMatcher = mAuthorPattern.matcher(line);
            Matcher timeMatcher = mTimePattern.matcher(line);

            if (revisionMatcher.matches())
            {
                int revId = Integer.parseInt( revisionMatcher.group(1) );
                revision = new Revision ( revId );
            }
            else if( authorMatcher.matches() )
                author = authorMatcher.group(1);
            else if( timeMatcher.matches() )
                dateString = timeMatcher.group(1);
            else if( line.compareTo(COMMENT) == 0 )
                state = ParseState.MESSAGE;
            else if( line.compareTo(ADDED) == 0 )
                state = ParseState.ADDED;
            else if( line.compareTo(MODIFIED) == 0 )
                state = ParseState.MODIFIED;
            else if( line.compareTo(REMOVED) == 0 )
                state = ParseState.REMOVED;
            else
            {
                switch(state)
                {
                case MESSAGE:
                    message += " " + line;
                    break;
                case ADDED:
                case MODIFIED:
                case REMOVED:
                    changes.add(new FileChange(line,
                                               revision,
                                               convertAction(state)));
                    break;
                case MOVED:
                    Matcher movedMatcher = mMovedPattern.matcher(line);
                    if( movedMatcher.matches() )
                        changes.add(new FileChange(line,
                                                   revision,
                                                   convertAction(state)));

                    break;
                default:
                    break;
                }
            }
        }

        result.add(new Changelist(revision, parseDate(dateString), author, message, changes));
    }

    private static Action convertAction(ParseState state)
    {
        switch(state)
        {
        case ADDED:
            return FileChange.Action.ADD;
        case MODIFIED:
            return FileChange.Action.EDIT;
        case REMOVED:
            return FileChange.Action.DELETE;
        case RENAMED:
            return FileChange.Action.MOVE;
        default:
            return FileChange.Action.UNKNOWN;
        }
    }

    private static long parseDate(String dateString) throws ParsingException
    {
        // timestamps look like 'Tue 2011-09-06 15:01:35 +0100'
        SimpleDateFormat dateFormat = new SimpleDateFormat("E yyyy-MM-dd HH:mm:ss Z");
        try
        {
            return dateFormat.parse(dateString).getTime();
        }
        catch (ParseException e)
        {
            throw new ParsingException("Unparseable date '" + dateString + "'");
        }
    }
}
