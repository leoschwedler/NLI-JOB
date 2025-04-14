package it.divitech.nliticketapp.httpclients.validation;

import java.util.ArrayList;
import java.util.List;

import it.divitech.nliticketapp.data.ticketing.Fee;
import it.divitech.nliticketapp.data.ticketing.Zone;

public class SearchTicketResponse
{
    public int id = 0;
    public Integer  unit_id = null;
    public String rom_code = null;
    public String extension = null;
    public String type = null;
    public int agency_id = 0;
    public int op_session_id = 0;
    public String order_uuid = null;
    public String uuid = null;
    public String parent_uuid = null;
    public int fee_id = 0;
    public int user_id = 0;
    public int from_zone_id = 0;
    public int to_zone_id = 0;
    public Integer from_stop_id = null;
    public Integer to_stop_id = null;
    public String valid_from = null;
    public String valid_to = null;
    public int minutes = 0;
    public int trips_count = 0;
    public int quantity = 0;
    public String details = null;
    public int value = 0;
    public int discount_value = 0;
    public String discount_reason= null;
    public int payment_id = 0;
    public int traveler_id = 0;
    public String media_type = null;
    public String media_hwid = null;
    public String ts = null;
    public String version = null;
    public State state = null;
    public Fee fee = null;
    public Zone from_zone = null;
    public Zone to_zone = null;
    public List<SearchTicketResponse> childs = new ArrayList<>();

    public static class State
    {
        public String document_id;
        public String status;
        public String ts_last_validation;
        public String ts_first_validation;
        public String ts_valid_from;
        public String ts_valid_to;
        public int residual_trips;
        public String updated_at;
        public String voided_at;
        public boolean voidable;
    }
}
