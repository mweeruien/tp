package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DASH_DAY_SHIFT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DASH_TIME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.ParserUtil.arePrefixesPresent;
import static seedu.address.logic.parser.ParserUtil.parseDayOfWeekAndSlot;
import static seedu.address.logic.parser.ParserUtil.parseDayOfWeekAndTime;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import seedu.address.logic.commands.ViewShiftCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Period;


/**
 * Class representing the find schedule command parser.
 */
public class ViewShiftCommandParser implements Parser<ViewShiftCommand> {

    public static final String INVALID_VIEW_SHIFT_COMMAND =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewShiftCommand.HELP_MESSAGE);
    public static final ParseException INVALID_VIEW_SHIFT_COMMAND_EXCEPTION =
            new ParseException(INVALID_VIEW_SHIFT_COMMAND);

    private DayOfWeek currDayOfWeek = DayOfWeek.from(LocalDate.now());
    private LocalTime currTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
    public final ViewShiftCommand errorCommand = new ViewShiftCommand(currDayOfWeek,
            ViewShiftCommand.INVALID_SLOT_NUMBER_INDICATING_EMPTY_PREFIXES, currTime,
            new Period(LocalDate.now()));

    @Override
    public ViewShiftCommand parse(String args) throws ParseException {
        // If it is empty, return a viewShift with the current day and time
        if (args.trim().equals("")) {
            return errorCommand;
        }

        requireNonNull(args);
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_DASH_DAY_SHIFT, PREFIX_DASH_TIME, PREFIX_DATE);
        checkPrefixes(argMultimap);

        int slotNum = ViewShiftCommand.INVALID_SLOT_NUMBER;
        DayOfWeek dayOfWeek = null; // should not be null when ViewShiftCommand object is created
        LocalTime time = null;
        LocalDate[] dates = new LocalDate[2];
        dates[0] = LocalDate.now();
        dates[1] = dates[0].plusDays(7);
        try {
            // remove the prefix, then parse
            if (argMultimap.getValue(PREFIX_DASH_TIME).isPresent()) {
                String trimmedArgs = args.replace(PREFIX_DASH_TIME.toString(), "").trim();
                String parsedArg = parseDayOfWeekAndTime(trimmedArgs);
                String[] parsedArgArray = parsedArg.split("-");
                dayOfWeek = DayOfWeek.valueOf(parsedArgArray[0].toUpperCase());
                time = LocalTime.parse(parsedArgArray[1], DateTimeFormatter.ofPattern("HH:mm"));
                // slotNum will remain null
            }

            if (argMultimap.getValue(PREFIX_DASH_DAY_SHIFT).isPresent()) {
                String trimmedArgs = args.replace(PREFIX_DASH_DAY_SHIFT.toString(), "").trim();
                String parsedArg = parseDayOfWeekAndSlot(trimmedArgs); // returns [day]-[slot]
                String[] parsedArgArray = parsedArg.split("-");
                dayOfWeek = DayOfWeek.valueOf(parsedArgArray[0].toUpperCase());
                slotNum = Integer.parseInt(parsedArgArray[1]);
                // time remains as INVALID_SLOT_NUMBER
            }
            if (argMultimap.getValue(PREFIX_DATE).isPresent()) {
                dates = ParserUtil.extractTupleDates(argMultimap);
            }

        } catch (ParseException pe) {
            throw INVALID_VIEW_SHIFT_COMMAND_EXCEPTION;
        }

        return new ViewShiftCommand(dayOfWeek, slotNum, time, new Period(dates[0], dates[1]));
    }

    private void checkPrefixes(ArgumentMultimap argMultimap) throws ParseException {
        // Exactly one of PREFIX_DASH_DAY_SHIFT or PREFIX_DASH_TIME must exist
        if (!arePrefixesPresent(argMultimap, PREFIX_DASH_DAY_SHIFT)
                && !arePrefixesPresent(argMultimap, PREFIX_DASH_TIME)) {
            throw INVALID_VIEW_SHIFT_COMMAND_EXCEPTION;
        }
        if (arePrefixesPresent(argMultimap, PREFIX_DASH_TIME)
                && arePrefixesPresent(argMultimap, PREFIX_DASH_DAY_SHIFT)) {
            throw INVALID_VIEW_SHIFT_COMMAND_EXCEPTION;
        }
    }
}
