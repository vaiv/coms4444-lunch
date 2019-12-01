var monkey_imgs = [];
var geese_imgs = [];
var family_imgs = [];
var food_imgs = [];
var bg_img;
var nest_img;
var monkeys;
var geese;
var family;
var food;
var first = true;
var total_score = 0;

var sandwich_imgs = [];
var eaten_sandwich_imgs = [];

var fruit_imgs = [];
var eaten_fruit_imgs = [];

var egg_imgs = [];
var eaten_egg_imgs = [];

var cookie_imgs = [];
var eaten_cookie_imgs = [];

var bag_imgs = [];

var status_cont;
var player_statuses = [];
var food_list = ['e', 'c', 's_1', 's_2', 'f_1', 'f_2'];

var food_state = [];
const EATEN = 1;
const STOLEN = -1;
const STORED = 0;
const EATING = 2;

function init(monkeys,geese,family,food) {

    bg_img = new Image();
    nest_img = new Image();
    nest_img.src = 'nest.png';
    bg_img.src = 'background3.png';

    for (var i = 0; i < monkeys.length; i++) 
    {
        var monkey = monkeys[i];
        var monkey_img = new Image();
        monkey_img.src = 'monkey.png';
        monkey_imgs.push(monkey_img);
    }

    for (var i = 0; i < geese.length; i++) 
    {
        var goose = geese[i];
        var goose_img = new Image();
        goose_img.src = 'goose.png';
        geese_imgs.push(goose_img);
    }

    for (var i = 0; i < family.length; i++) 
    {
        var member = family[i];
        var member_img = new Image();
	var j = i%9;
        //member_img.src = family[i].avatars+ '_'+(j+1).toString() + '.png';
        member_img.src = 'flintstone_'+(j+1).toString() + '.png';
        family_imgs.push(member_img);
    }

    for(var i=0;i<100;i++)
    {
        var sandwich_img = new Image();
        var eaten_sandwich_img = new Image();
        var fruit_img = new Image();
        var eaten_fruit_img = new Image();
        var egg_img = new Image();
        var eaten_egg_img = new Image();
        var cookie_img = new Image();
        var eaten_cookie_img = new Image();
        var bag_img = new Image();

        sandwich_img.src = 'sandwich.png';
        eaten_sandwich_img.src = 'sandwich.png';
        egg_img.src = 'egg.png';
        eaten_egg_img.src = 'egg_half.png';
        fruit_img.src ='orange_full.png';
        eaten_fruit_img.src = 'orange_half.png';
        cookie_img.src = 'cookie.png';
        eaten_cookie_img.src = 'eaten_cookie.png';
        bag_img.src ='bag.png';

        sandwich_imgs.push(sandwich_img);
        eaten_sandwich_imgs.push(eaten_sandwich_img);
        egg_imgs.push(egg_img);
        eaten_egg_imgs.push(eaten_egg_img);
        fruit_imgs.push(fruit_img);
        eaten_fruit_imgs.push(eaten_fruit_img);
        cookie_imgs.push(cookie_img);
        eaten_cookie_imgs.push(eaten_cookie_img);
        bag_imgs.push(bag_img);
    }
    
    initPlayerStatus(family);
  
  window.requestAnimationFrame(draw);
}

function initPlayerStatus(family) {
    var tpl = document.getElementById('player-status-x');
    status_cont = tpl.parentNode;
    
    for (var i = 0; i < family.length; i++) 
    {
        var si = tpl.cloneNode(true);
        player_statuses.push(si);
        status_cont.appendChild(si);
        si.id = 'ps-' + i;
        si.classList.remove('d-none');
        si.getElementsByClassName('ps-img')[0].src = 'flintstone' + '_'+(i+1).toString() + '.png';
        si.getElementsByClassName('ps-gid')[0].innerText = family[i].name;
        //member_img.src = family[i].avatars+ '_'+(j+1).toString() + '.png';
        //family_imgs.push(member_img);
        var fStatus = {};
        food_state.push(fStatus);
        for (var j = 0; j < food_list.length; j++) {
            fStatus[food_list[j]] = {percentage: 0, status: STORED};
        }
        fStatus['eating'] = null;
    }
}

function draw()
{
    canvas = document.getElementById('canvas');
    ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    drawBorder(ctx);
    drawStats(ctx);


    ctx.drawImage(nest_img,1220,1220,80, 80*nest_img.height/nest_img.width);

    for (var i = 0; i < monkeys.length; i++) {
        var monkey = monkeys[i];
        var drawX = ((monkey.x + 50) /100) * 1200;
        var drawY = ((monkey.y + 50) /100) * 1200;
        var aspect_ratio = 1.248;
        ctx.drawImage(monkey_imgs[i],drawX,drawY,60, 60/aspect_ratio);
        
    }


    for (var i = 0; i < geese.length; i++) {
        var goose = geese[i];
        var drawX = ((goose.x + 50) /100) * 1200;
        var drawY = ((goose.y + 50) /100) * 1200;
        var aspect_ratio = 1.659;
        ctx.drawImage(geese_imgs[i],drawX,drawY,60, 60/aspect_ratio);
      }


    for (var i = 0; i < family.length; i++) {
        var member = family[i];
        var drawX = ((member.x + 50) /100) * 1200;
        var drawY = ((member.y + 50) /100) * 1200;
        // var aspect_ratio = 0.515;
        var img_size = 100;

        // if((i%4)+1==3)
        //     {
        //         aspect_ratio = 0.879;
        //         img_size = 80;
        //     }
        // else if ((i%4)+1==4)
        //     {
        //         aspect_ratio = 1.132;
        //         img_size = 80;
        //     }
        // else if((i%4)==0)
        // {
        //     aspect_ratio = 0.515;
        //     img_size = 60;
        // }

        ctx.drawImage(family_imgs[i],drawX,drawY,img_size*family_imgs[i].width/family_imgs[i].height, img_size);
       
    }

    for(var i=0;i<food.length;i++)
    {
        var item = food[i];
        var drawX = ((item.x + 50) /100) * 1200;
        var drawY = ((item.y + 50) /100) * 1200;
        var id = parseInt(item.id);
        var iterator = new Array(9).fill(0);

        if(drawX<0 && drawY<0)
            continue;
        switch(id)
        {
            case 1: ctx.drawImage(sandwich_imgs[iterator[0]],drawX,drawY,35, sandwich_imgs[iterator[0]].height*35/sandwich_imgs[iterator[0]].width); iterator[0]++; break;
            case 2: ctx.drawImage(eaten_sandwich_imgs[iterator[1]],drawX,drawY,20, eaten_sandwich_imgs[iterator[1]].height*20/eaten_sandwich_imgs[iterator[1]].width);iterator[1]++; break;
            case 3: ctx.drawImage(fruit_imgs[iterator[2]],drawX,drawY,20, fruit_imgs[iterator[2]].height*20/fruit_imgs[iterator[2]].width);iterator[2]++; break;
            case 4: ctx.drawImage(eaten_fruit_imgs[iterator[3]],drawX,drawY,20, eaten_fruit_imgs[iterator[3]].height*20/eaten_fruit_imgs[iterator[3]].width); iterator[3]++; break;
            case 5: ctx.drawImage(egg_imgs[iterator[4]],drawX,drawY,20, egg_imgs[iterator[4]].height*20/egg_imgs[iterator[4]].width); iterator[4]++; break;
            case 6: ctx.drawImage(eaten_egg_imgs[iterator[5]],drawX,drawY,20, eaten_egg_imgs[iterator[5]].height*20/eaten_egg_imgs[iterator[5]].width); iterator[5]++; break;
            case 7: ctx.drawImage(cookie_imgs[iterator[6]],drawX,drawY,35, cookie_imgs[iterator[6]].height*35/cookie_imgs[iterator[6]].width); iterator[6]++; break;
            case 8: ctx.drawImage(eaten_cookie_imgs[iterator[7]],drawX,drawY,15, eaten_cookie_imgs[iterator[7]].height*15/eaten_cookie_imgs[iterator[7]].width); iterator[7]++; break;
            case 9: ctx.drawImage(bag_imgs[iterator[8]],drawX,drawY,40, bag_imgs[iterator[8]].height*40/bag_imgs[iterator[8]].width);iterator[8]++; break;
        }

    }

      window.requestAnimationFrame(draw);



}


function drawBorder(ctx)
{
    ctx.beginPath();
    ctx.lineWidth="4";
    ctx.strokeStyle="black";
    ctx.fillStyle="white";
    ctx.rect(0,0,1250,1250);
    ctx.drawImage(bg_img,0,0,1250,1250);
    ctx.stroke();
}

function drawMonkeys(ctx, monkeys) {
    for (var i = 0; i < monkeys.length; i++) {
        var monkey = monkeys[i];
        var img=new Image();
        img.addEventListener('load', function() {
            // execute drawImage statements here
            var drawX = ((monkey.x + 50) /100) * 1200;
            var drawY = ((monkey.y + 50) /100) * 1200;
            var aspect_ratio = 1.248;
            ctx.drawImage(img,drawX,drawY,80, 80/aspect_ratio);
            }, false);
        img.src='monkey.png';
        //console.log(bale);
        
    }
}

function drawGeese(ctx, geese) {
    for (var i = 0; i < geese.length; i++) {
        var goose = geese[i];
        var img=new Image();
        img.addEventListener('load', function() {
            // execute drawImage statements here
            var drawX = ((goose.x + 50) /100) * 1200;
            var drawY = ((goose.y + 50) /100) * 1200;
            var aspect_ratio = 1.659;
            ctx.drawImage(img,drawX,drawY,100, 100/aspect_ratio);
        }, false);
        img.src='goose.png';

        //console.log(bale);
        
    }
}

function drawFamily(ctx, family) {
    for (var i = 0; i < family.length; i++) {
        var member = family[i];
        var img=new Image();

        img.addEventListener('load', function() {
            // execute drawImage statements here
            var drawX = ((member.x + 50) /100) * 1200;
            var drawY = ((member.y + 50) /100) * 1200;
            var aspect_ratio = 0.515;

            ctx.drawImage(img,drawX,drawY,100*img.width/img.height, 100);

             }, false);
        img.src='jetson_'+((i%4)+1).toString()+'.png';
        //console.log(bale);
       
    }
}

function drawStats() {
    total_score = 0;
    for (var i = 0; i < family.length; i++) {
        var p = family[i];
        var ps = player_statuses[i];
        ps.getElementsByClassName("ps-score")[0].innerText = p.score;
        var eating = null;
        var fStatus = food_state[i];
        switch (p.eating) {
            case 'SANDWICH1' : eating = 's_1'; break;
            case 'SANDWICH2' : eating = 's_2'; break;
            case 'FRUIT1' : eating = 'f_1'; break;
            case 'FRUIT2' : eating = 'f_2'; break;
            case 'COOKIE' : eating = 'c'; break;
            case 'EGG' : eating = 'e'; break;
        }
        
        // check for food that was being eaten and now is gone
        if (fStatus.eating !== null){
            if (!p[fStatus.eating]) {
                if (fStatus[fStatus.eating].percentage > 98.2) {
                    fStatus[fStatus.eating].percentage = 100;
                    fStatus[fStatus.eating].status = EATEN;
                } else {
                    fStatus[fStatus.eating].status = STOLEN;
                }
            } else if (eating === null) {
                fStatus[fStatus.eating].status = STORED;
            }
        }
        if (fStatus.eating !== null)
            updateFoodStatus(i, fStatus.eating);
        
        fStatus['eating'] = eating;
        if (fStatus['eating'] !== null) {
            var percent_rem = 100 - parseFloat(p.rem_time).toFixed(2);
            percent_rem = percent_rem.toFixed(2);
            if(percent_rem>100)
                percent_rem = 100;
            fStatus[fStatus.eating].percentage = percent_rem;
            fStatus[fStatus.eating].status = EATING;
            updateFoodStatus(i, fStatus.eating);
        }
        
        if(!isNaN(p.score) && p.score>0)
            total_score+= p.score;
    }
    document.getElementById("total-score").innerHTML = total_score;
}

function updateFoodStatus(member, food) {
    var ps = player_statuses[member];
    var foodStatus = ps.getElementsByClassName("ps-food-" + food)[0];
    var colorCl = 'default';
    switch (food_state[member][food].status) {
        case STORED: colorCl = 'secondary'; break;
        case EATEN: colorCl = 'success'; break;
        case STOLEN: colorCl = 'danger'; break;
        case EATING: colorCl = 'primary'; break;
    }
    //foodStatus.previousElementSibling.classList.remove("bg-secondary", "bg-success", "bg-danger");
    //foodStatus.previousElementSibling.classList.add("bg-" + colorCl);
    //foodStatus.classList.remove("bg-secondary", "bg-success", "bg-danger");
    //foodStatus.classList.add("bg-" + colorCl);
    var progress = foodStatus.getElementsByClassName("progress-bar")[0];
    progress.classList.remove("bg-secondary", "bg-success", "bg-danger");
    progress.classList.add("bg-" + colorCl);
    if (food_state[member][food].status !== STOLEN) {
        var percent_rem = food_state[member][food].percentage;
        if(percent_rem>100)
            percent_rem = 100;
        progress.style.width = percent_rem + "%";
        progress.innerText = percent_rem + "%";
    } else {
        progress.style.width = 100 + "%";
        progress.innerText = "Stolen";
    }
}

function drawStats1(ctx)
{
    var colors = ["blue", "darkblue", "darkgreen", "darkmagenta", "salmon", "gold", "deeppink"]
    total_score = 0;
    for (var i = 0; i < family.length; i++)
    {
        var j = i%4;
        var p = family[i];
        ctx.fillStyle=colors[j];

        ctx.font = "20px Arial";
        var items_available = '';
        var avatar = 'George Jetson';

        if(i%4==1)
            avatar = 'Jane Jetson';
        else if (i%4==2)
            avatar = 'Judy Jetson';
        else if (i%4==3)
            avatar = 'Elroy Jetson';


        if(p.avatars!='jetson')
            avatar = p.avatars+'_'+i.toString();


        if(p.s_1)
            items_available+='sandwich_1, ';
        if(p.s_2)
            items_available+= 'sandwich_2, ';
        if(p.f_1)
            items_available+='fruit_1, ';
        if(p.f_2)
            items_available+= 'fruit_2, ';
        if(p.e)
            items_available+= 'egg, ';
        if(p.c)
            items_available+= 'cookie, ';

        ctx.fillText(p.name + ': ' +  avatar,1280,150*i + 15);
        var percent_rem = 100 - parseFloat(p.rem_time).toFixed(2);
        percent_rem = percent_rem.toFixed(2);
        if(percent_rem>100)
            percent_rem = NaN;

        if(!isNaN(p.score) && p.score>0)
            total_score+= p.score
        ctx.fillText('score: ' + p.score + ' eating: ' + p.eating + ' (' + percent_rem + '%) ',1280, 150*i + 50);
        ctx.fillText('items in bag: ' + items_available ,1280,150*i + 100);
    }
    ctx.fillStyle=colors[2];
    ctx.fillText('total_score: ' + total_score, 1350, 1280);
}

function drawLine(ctx, x_start, y_start, x_end, y_end) {
    ctx.beginPath();
    ctx.moveTo(x_start, y_start);
    ctx.strokeStyle="black";
    ctx.lineTo(x_end, y_end);
    ctx.stroke();
}

var y_pos = 40;

function process(data) {
    var result = JSON.parse(data)

    // console.log(result);
    var refresh = parseFloat(result.refresh);
    monkeys = result.monkey_locations;
    geese = result.geese_locations;
    family = result.player_locations;
    food = result.food_locations;
    var player1 = result.player1;
    var player2 = result.player2;
    // console.log(result.food_locations);

    if(first)
    {
        init(monkeys,geese,family,food);
        first = false;
    }
     timeElement = document.getElementById('time');
    timeElement.innerHTML = result.remaining_time;

    return refresh;
}

var latest_version = -1;

function ajax(version, retries, timeout) {
    // console.log("Version " + version);
    var xhttp = new XMLHttpRequest();
    monkey_img = new Image();
    monkey_img.src = 'monkey.png';
    goose_img = new Image();
    goose_img.src = 'goose.png';
    xhttp.onload = (function() {
            var refresh = -1;
            try {
                if (xhttp.readyState != 4)
                    throw "Incomplete HTTP request: " + xhttp.readyState;
                if (xhttp.status != 200)
                    throw "Invalid HTTP status: " + xhttp.status;
                //console.log(xhttp.responseText);
                refresh = process(xhttp.responseText);
                if (latest_version < version)
                    latest_version = version;
                else refresh = -1;
            } catch (message) {
                alert(message);
            }

            // console.log(refresh);
            if (refresh >= 0)
                setTimeout(function() { ajax(version + 1, 10, 100); }, refresh);
        });
    xhttp.onabort = (function() { location.reload(true); });
    xhttp.onerror = (function() { location.reload(true); });
    xhttp.ontimeout = (function() {
            if (version <= latest_version)
                console.log("AJAX timeout (version " + version + " <= " + latest_version + ")");
            else if (retries == 0)
                location.reload(true);
            else {
                console.log("AJAX timeout (version " + version + ", retries: " + retries + ")");
                ajax(version, retries - 1, timeout * 2);
            }
        });
    xhttp.open("GET", "data.txt", true);
    xhttp.responseType = "text";
    xhttp.timeout = timeout;
    xhttp.send();
}

ajax(1, 10, 100);

// process('{"refresh":0, "grp_a":"g1", "grp_b":"g2", "grp_a_round":"1,2,3,4,5", "grp_b_round":"4,5,6,7,8",' +
//    '"grp_a_skills":"4,3,2,5,6,7,3", "grp_b_skills":"2,3,5,7,5,4,3", "grp_a_dist":"1,2;4,3;6,7", "grp_b_dist":"3,4;5,1;8,7", ' +
//    '"grp_a_score":"3", "grp_b_score":"0"}');
