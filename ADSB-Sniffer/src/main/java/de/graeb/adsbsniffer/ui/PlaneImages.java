package de.graeb.adsbsniffer.ui;

import de.graeb.adsbsniffer.R;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 * @author markus
 */
public class PlaneImages {
    public static int lookupAircraft(String icao24) {
        byte[] id;
        try {
            id = Hex.decodeHex(icao24.toCharArray());
        } catch (DecoderException e) {
            return R.drawable.flag_empty;
        }
        int nr = toInt(id);


        if (7340032 <= nr && 7344127 >= nr) {
            return R.drawable.af;
        } else if (5246976 <= nr && 5247999 >= nr) {
            return R.drawable.al;
        } else if (655360 <= nr && 688127 >= nr) {
            return R.drawable.dz;
        } else if (589824 <= nr && 593919 >= nr) {
            return R.drawable.ao;
        } else if (827392 <= nr && 828415 >= nr) {
            return R.drawable.ag;
        } else if (14680064 <= nr && 14942207 >= nr) {
            return R.drawable.ar;
        } else if (6291456 <= nr && 6292479 >= nr) {
            return R.drawable.am;
        } else if (8126464 <= nr && 8388607 >= nr) {
            return R.drawable.au;
        } else if (4456448 <= nr && 4489215 >= nr) {
            return R.drawable.at;
        } else if (6293504 <= nr && 6294527 >= nr) {
            return R.drawable.az;
        } else if (688128 <= nr && 692223 >= nr) {
            return R.drawable.bs;
        } else if (8994816 <= nr && 8998911 >= nr) {
            return R.drawable.bh;
        } else if (7348224 <= nr && 7352319 >= nr) {
            return R.drawable.bd;
        } else if (696320 <= nr && 697343 >= nr) {
            return R.drawable.bb;
        } else if (5308416 <= nr && 5309439 >= nr) {
            return R.drawable.by;
        } else if (4489216 <= nr && 4521983 >= nr) {
            return R.drawable.be;
        } else if (700416 <= nr && 701439 >= nr) {
            return R.drawable.bz;
        } else if (606208 <= nr && 607231 >= nr) {
            return R.drawable.bj;
        } else if (6815744 <= nr && 6816767 >= nr) {
            return R.drawable.bt;
        } else if (15286272 <= nr && 15290367 >= nr) {
            return R.drawable.bo;
        } else if (5320704 <= nr && 5321727 >= nr) {
            return R.drawable.ba;
        } else if (196608 <= nr && 197631 >= nr) {
            return R.drawable.bw;
        } else if (14942208 <= nr && 15204351 >= nr) {
            return R.drawable.br;
        } else if (8998912 <= nr && 8999935 >= nr) {
            return R.drawable.bn;
        } else if (4521984 <= nr && 4554751 >= nr) {
            return R.drawable.bg;
        } else if (638976 <= nr && 643071 >= nr) {
            return R.drawable.bf;
        } else if (204800 <= nr && 208895 >= nr) {
            return R.drawable.bi;
        } else if (7397376 <= nr && 7401471 >= nr) {
            return R.drawable.kh;
        } else if (212992 <= nr && 217087 >= nr) {
            return R.drawable.cm;
        } else if (12582912 <= nr && 12845055 >= nr) {
            return R.drawable.ca;
        } else if (614400 <= nr && 615423 >= nr) {
            return R.drawable.cv;
        } else if (442368 <= nr && 446463 >= nr) {
            return R.drawable.cf;
        } else if (540672 <= nr && 544767 >= nr) {
            return R.drawable.td;
        } else if (15204352 <= nr && 15208447 >= nr) {
            return R.drawable.cl;
        } else if (7864320 <= nr && 8126463 >= nr) {
            return R.drawable.cn;
        } else if (704512 <= nr && 708607 >= nr) {
            return R.drawable.co;
        } else if (217088 <= nr && 218111 >= nr) {
            return R.drawable.km;
        } else if (221184 <= nr && 225279 >= nr) {
            return R.drawable.cg;
        } else if (9441280 <= nr && 9442303 >= nr) {
            return R.drawable.ck;
        } else if (712704 <= nr && 716799 >= nr) {
            return R.drawable.cr;
        } else if (229376 <= nr && 233471 >= nr) {
            return R.drawable.ci;
        } else if (5250048 <= nr && 5251071 >= nr) {
            return R.drawable.hr;
        } else if (720896 <= nr && 724991 >= nr) {
            return R.drawable.cu;
        } else if (5013504 <= nr && 5014527 >= nr) {
            return R.drawable.cy;
        } else if (4816896 <= nr && 4849663 >= nr) {
            return R.drawable.cz;
        } else if (7471104 <= nr && 7503871 >= nr) {
            return R.drawable.kp;
        } else if (573440 <= nr && 577535 >= nr) {
            return R.drawable.flag_empty;
        } else if (4554752 <= nr && 4587519 >= nr) {
            return R.drawable.dk;
        } else if (622592 <= nr && 623615 >= nr) {
            return R.drawable.dj;
        } else if (802816 <= nr && 806911 >= nr) {
            return R.drawable.do_;
        } else if (15220736 <= nr && 15224831 >= nr) {
            return R.drawable.ec;
        } else if (65536 <= nr && 98303 >= nr) {
            return R.drawable.eg;
        } else if (729088 <= nr && 733183 >= nr) {
            return R.drawable.sv;
        } else if (270336 <= nr && 274431 >= nr) {
            return R.drawable.gq;
        } else if (2105344 <= nr && 2106367 >= nr) {
            return R.drawable.er;
        } else if (5312512 <= nr && 5313535 >= nr) {
            return R.drawable.ee;
        } else if (262144 <= nr && 266239 >= nr) {
            return R.drawable.et;
        } else if (13139968 <= nr && 13144063 >= nr) {
            return R.drawable.fj;
        } else if (4587520 <= nr && 4620287 >= nr) {
            return R.drawable.fi;
        } else if (3670016 <= nr && 3932159 >= nr) {
            return R.drawable.fr;
        } else if (253952 <= nr && 258047 >= nr) {
            return R.drawable.ga;
        } else if (630784 <= nr && 634879 >= nr) {
            return R.drawable.gm;
        } else if (5324800 <= nr && 5325823 >= nr) {
            return R.drawable.ge;
        } else if (3932160 <= nr && 4194303 >= nr) {
            return R.drawable.de;
        } else if (278528 <= nr && 282623 >= nr) {
            return R.drawable.gh;
        } else if (4620288 <= nr && 4653055 >= nr) {
            return R.drawable.gr;
        } else if (835584 <= nr && 836607 >= nr) {
            return R.drawable.gd;
        } else if (737280 <= nr && 741375 >= nr) {
            return R.drawable.gt;
        } else if (286720 <= nr && 290815 >= nr) {
            return R.drawable.gn;
        } else if (294912 <= nr && 295935 >= nr) {
            return R.drawable.gw;
        } else if (745472 <= nr && 749567 >= nr) {
            return R.drawable.gy;
        } else if (753664 <= nr && 757759 >= nr) {
            return R.drawable.ht;
        } else if (761856 <= nr && 765951 >= nr) {
            return R.drawable.hn;
        } else if (4653056 <= nr && 4685823 >= nr) {
            return R.drawable.hu;
        } else if (5029888 <= nr && 5033983 >= nr) {
            return R.drawable.is;
        } else if (8388608 <= nr && 8650751 >= nr) {
            return R.drawable.in;
        } else if (9043968 <= nr && 9076735 >= nr) {
            return R.drawable.id;
        } else if (7536640 <= nr && 7569407 >= nr) {
            return R.drawable.flag_empty;
        } else if (7503872 <= nr && 7536639 >= nr) {
            return R.drawable.iq;
        } else if (5021696 <= nr && 5025791 >= nr) {
            return R.drawable.ie;
        } else if (7569408 <= nr && 7602175 >= nr) {
            return R.drawable.il;
        } else if (3145728 <= nr && 3407871 >= nr) {
            return R.drawable.it;
        } else if (778240 <= nr && 782335 >= nr) {
            return R.drawable.jm;
        } else if (8650752 <= nr && 8912895 >= nr) {
            return R.drawable.jp;
        } else if (7602176 <= nr && 7634943 >= nr) {
            return R.drawable.jo;
        } else if (6828032 <= nr && 6829055 >= nr) {
            return R.drawable.kz;
        } else if (311296 <= nr && 315391 >= nr) {
            return R.drawable.ke;
        } else if (13164544 <= nr && 13165567 >= nr) {
            return R.drawable.ki;
        } else if (7364608 <= nr && 7368703 >= nr) {
            return R.drawable.kw;
        } else if (6295552 <= nr && 6296575 >= nr) {
            return R.drawable.kg;
        } else if (7372800 <= nr && 7376895 >= nr) {
            return R.drawable.la;
        } else if (5254144 <= nr && 5255167 >= nr) {
            return R.drawable.lv;
        } else if (7634944 <= nr && 7667711 >= nr) {
            return R.drawable.lb;
        } else if (303104 <= nr && 304127 >= nr) {
            return R.drawable.ls;
        } else if (327680 <= nr && 331775 >= nr) {
            return R.drawable.lr;
        } else if (98304 <= nr && 131071 >= nr) {
            return R.drawable.ly;
        } else if (5258240 <= nr && 5259263 >= nr) {
            return R.drawable.lt;
        } else if (5046272 <= nr && 5047295 >= nr) {
            return R.drawable.lu;
        } else if (344064 <= nr && 348159 >= nr) {
            return R.drawable.mg;
        } else if (360448 <= nr && 364543 >= nr) {
            return R.drawable.mw;
        } else if (7667712 <= nr && 7700479 >= nr) {
            return R.drawable.my;
        } else if (368640 <= nr && 369663 >= nr) {
            return R.drawable.mv;
        } else if (376832 <= nr && 380927 >= nr) {
            return R.drawable.ml;
        } else if (5054464 <= nr && 5055487 >= nr) {
            return R.drawable.mt;
        } else if (9437184 <= nr && 9438207 >= nr) {
            return R.drawable.mh;
        } else if (385024 <= nr && 386047 >= nr) {
            return R.drawable.mr;
        } else if (393216 <= nr && 394239 >= nr) {
            return R.drawable.mu;
        } else if (851968 <= nr && 884735 >= nr) {
            return R.drawable.mx;
        } else if (6819840 <= nr && 6820863 >= nr) {
            return R.drawable.fm;
        } else if (5062656 <= nr && 5063679 >= nr) {
            return R.drawable.id;
        } else if (6823936 <= nr && 6824959 >= nr) {
            return R.drawable.mn;
        } else if (5332992 <= nr && 5334015 >= nr) {
            return R.drawable.me;
        } else if (131072 <= nr && 163839 >= nr) {
            return R.drawable.ma;
        } else if (24576 <= nr && 28671 >= nr) {
            return R.drawable.mz;
        } else if (7356416 <= nr && 7360511 >= nr) {
            return R.drawable.mm;
        } else if (2101248 <= nr && 2102271 >= nr) {
            return R.drawable.na;
        } else if (13148160 <= nr && 13149183 >= nr) {
            return R.drawable.nr;
        } else if (7380992 <= nr && 7385087 >= nr) {
            return R.drawable.np;
        } else if (4718592 <= nr && 4751359 >= nr) {
            return R.drawable.nl;
        } else if (13107200 <= nr && 13139967 >= nr) {
            return R.drawable.nz;
        } else if (786432 <= nr && 790527 >= nr) {
            return R.drawable.ni;
        } else if (401408 <= nr && 405503 >= nr) {
            return R.drawable.ne;
        } else if (409600 <= nr && 413695 >= nr) {
            return R.drawable.ng;
        } else if (4685824 <= nr && 4718591 >= nr) {
            return R.drawable.no;
        } else if (7389184 <= nr && 7390207 >= nr) {
            return R.drawable.om;
        } else if (7733248 <= nr && 7766015 >= nr) {
            return R.drawable.pk;
        } else if (6832128 <= nr && 6833151 >= nr) {
            return R.drawable.pw;
        } else if (794624 <= nr && 798719 >= nr) {
            return R.drawable.pa;
        } else if (9011200 <= nr && 9015295 >= nr) {
            return R.drawable.pg;
        } else if (15237120 <= nr && 15241215 >= nr) {
            return R.drawable.py;
        } else if (15253504 <= nr && 15257599 >= nr) {
            return R.drawable.pe;
        } else if (7700480 <= nr && 7733247 >= nr) {
            return R.drawable.ph;
        } else if (4751360 <= nr && 4784127 >= nr) {
            return R.drawable.pl;
        } else if (4784128 <= nr && 4816895 >= nr) {
            return R.drawable.pt;
        } else if (434176 <= nr && 435199 >= nr) {
            return R.drawable.qa;
        } else if (7438336 <= nr && 7471103 >= nr) {
            return R.drawable.kr;
        } else if (5262336 <= nr && 5263359 >= nr) {
            return R.drawable.md;
        } else if (4849664 <= nr && 4882431 >= nr) {
            return R.drawable.ro;
        } else if (1048576 <= nr && 2097151 >= nr) {
            return R.drawable.ru;
        } else if (450560 <= nr && 454655 >= nr) {
            return R.drawable.rw;
        } else if (13156352 <= nr && 13157375 >= nr) {
            return R.drawable.lc;
        } else if (770048 <= nr && 771071 >= nr) {
            return R.drawable.vc;
        } else if (9445376 <= nr && 9446399 >= nr) {
            return R.drawable.ws;
        } else if (5242880 <= nr && 5243903 >= nr) {
            return R.drawable.sm;
        } else if (647168 <= nr && 648191 >= nr) {
            return R.drawable.st;
        } else if (7405568 <= nr && 7438335 >= nr) {
            return R.drawable.sa;
        } else if (458752 <= nr && 462847 >= nr) {
            return R.drawable.sn;
        } else if (4980736 <= nr && 5013503 >= nr) {
            return R.drawable.rs;
        } else if (475136 <= nr && 476159 >= nr) {
            return R.drawable.sc;
        } else if (483328 <= nr && 484351 >= nr) {
            return R.drawable.sl;
        } else if (7766016 <= nr && 7798783 >= nr) {
            return R.drawable.sg;
        } else if (5266432 <= nr && 5267455 >= nr) {
            return R.drawable.sk;
        } else if (5270528 <= nr && 5271551 >= nr) {
            return R.drawable.si;
        } else if (9007104 <= nr && 9008127 >= nr) {
            return R.drawable.sb;
        } else if (491520 <= nr && 495615 >= nr) {
            return R.drawable.so;
        } else if (32768 <= nr && 65535 >= nr) {
            return R.drawable.za;
        } else if (3407872 <= nr && 3670015 >= nr) {
            return R.drawable.es;
        } else if (7798784 <= nr && 7831551 >= nr) {
            return R.drawable.lk;
        } else if (507904 <= nr && 511999 >= nr) {
            return R.drawable.sd;
        } else if (819200 <= nr && 823295 >= nr) {
            return R.drawable.sr;
        } else if (499712 <= nr && 500735 >= nr) {
            return R.drawable.sz;
        } else if (4882432 <= nr && 4915199 >= nr) {
            return R.drawable.se;
        } else if (4915200 <= nr && 4947967 >= nr) {
            return R.drawable.ch;
        } else if (7831552 <= nr && 7864319 >= nr) {
            return R.drawable.sy;
        } else if (5328896 <= nr && 5329919 >= nr) {
            return R.drawable.tj;
        } else if (8912896 <= nr && 8945663 >= nr) {
            return R.drawable.th;
        } else if (5316608 <= nr && 5317631 >= nr) {
            return R.drawable.flag_empty;
        } else if (557056 <= nr && 561151 >= nr) {
            return R.drawable.tg;
        } else if (13160448 <= nr && 13161471 >= nr) {
            return R.drawable.to;
        } else if (811008 <= nr && 815103 >= nr) {
            return R.drawable.tt;
        } else if (163840 <= nr && 196607 >= nr) {
            return R.drawable.tn;
        } else if (4947968 <= nr && 4980735 >= nr) {
            return R.drawable.tr;
        } else if (6297600 <= nr && 6298623 >= nr) {
            return R.drawable.tm;
        } else if (425984 <= nr && 430079 >= nr) {
            return R.drawable.ug;
        } else if (5275648 <= nr && 5308415 >= nr) {
            return R.drawable.ua;
        } else if (9003008 <= nr && 9007103 >= nr) {
            return R.drawable.ae;
        } else if (4194304 <= nr && 4456447 >= nr) {
            return R.drawable.gb;
        } else if (524288 <= nr && 528383 >= nr) {
            return R.drawable.tz;
        } else if (10485760 <= nr && 11534335 >= nr) {
            return R.drawable.us;
        } else if (15269888 <= nr && 15273983 >= nr) {
            return R.drawable.uy;
        } else if (5274624 <= nr && 5275647 >= nr) {
            return R.drawable.uz;
        } else if (13172736 <= nr && 13173759 >= nr) {
            return R.drawable.vu;
        } else if (884736 <= nr && 917503 >= nr) {
            return R.drawable.ve;
        } else if (8945664 <= nr && 8978431 >= nr) {
            return R.drawable.vn;
        } else if (8978432 <= nr && 8982527 >= nr) {
            return R.drawable.ye;
        } else if (565248 <= nr && 569343 >= nr) {
            return R.drawable.zm;
        } else if (16384 <= nr && 17407 >= nr) {
            return R.drawable.zw;
        } else if (15728640 <= nr && 15761407 >= nr) {
            return R.drawable.flag_empty;
        } else if (9015296 <= nr && 9016319 >= nr) {
            return R.drawable.flag_empty;
        } else if (15765504 <= nr && 15766527 >= nr) {
            return R.drawable.flag_empty;
        }

        return R.drawable.flag_empty;
    }

    private static int toInt(byte[] bytes) {
        int number = 0;
        for (int i = 0; i < bytes.length * 8; i++) {
            byte b = bytes[bytes.length - 1 - i / 8];
            int mask = 1 << (i % 8);
            if ((b & mask) != 0) {
                number |= (1 << i);
            }
        }
        return number;
    }
}
