#include "algerium/navigation.h"
namespace algerium {
void SessionHistory::navigate(HistoryEntry e){if(!entries.empty()&&index+1<entries.size())entries.erase(entries.begin()+index+1,entries.end());entries.push_back(std::move(e));index=entries.size()-1;}
bool SessionHistory::can_go_back()const{return !entries.empty()&&index>0;}
bool SessionHistory::can_go_forward()const{return !entries.empty()&&index+1<entries.size();}
const HistoryEntry& SessionHistory::current()const{return entries.at(index);}
const HistoryEntry& SessionHistory::back(){if(can_go_back())--index;return current();}
const HistoryEntry& SessionHistory::forward(){if(can_go_forward())++index;return current();}
}

void algerium::SessionHistory::replace_current(HistoryEntry e){ if(entries.empty()){entries.push_back(std::move(e));index=0;}else entries[index]=std::move(e); }
