
$.ajaxSetup({
    beforeSend: function (xhr) {
        let token = $("meta[name='_csrf']").attr("content");
        xhr.setRequestHeader('X-CSRF-TOKEN', token);
    }
});

// Upload class starts here
var Upload = function (file) {
    this.file = file;
};

Upload.prototype.getType = function () {
    return this.file.type;
};
Upload.prototype.getSize = function () {
    return this.file.size;
};
Upload.prototype.getName = function () {
    return this.file.name;
};
Upload.prototype.doUpload = function () {
    var that = this;
    var formData = new FormData();
    $('#upload').off('click');

    // add assoc key values, this will be posts values
    formData.append("file", this.file, this.getName());
    formData.append("upload_file", true);

    $.ajax({
        type: "POST",
        url: "uploadFile",
        xhr: function () {
            var myXhr = $.ajaxSettings.xhr();
            if (myXhr.upload) {
                myXhr.upload.addEventListener('progress', that.progressHandling, false);
            }
            return myXhr;
        },
        success: function (data) {
            $('#msg').html(data);
        },
        error: function (error) {
            console.log(error);
        },
        async: true,
        data: formData,
        cache: false,
        contentType: false,
        processData: false,
        timeout: 60000
    });
};

Upload.prototype.progressHandling = function (event) {
    var percent = 0;
    var position = event.loaded || event.position;
    var total = event.total;
    var progress_bar_id = "#progress-wrp";
    if (event.lengthComputable) {
        percent = Math.ceil(position / total * 100);
    }
    // update progressbars classes so it fits your code
    $(progress_bar_id + " .progress-bar").css("width", +percent + "%");
    $(progress_bar_id + " .status").text(percent + "%");
};
// Upload class ends here

function fun_confirm() {
    if($('#confirm').is(":checked")) {
        $('#delete').removeClass('d-none');
    } else {
        $('#delete').addClass('d-none');
    }
}

function fetchAccountDetail(id) {
    $('#nav-profile-tab').trigger('click');
    $('#id').val(id);
    $('#idSearch').click();
}

function fetchProductDetail(id) {
    $('#nav-profile-tab').trigger('click');
    $('#id').val(id);
    $('#productIdSearch').click();
}

function getVoucherForm() {
    if ($("#voucherType").val() == "PURCHASE" || $("#voucherType").val() == "SALE") {
        $("#formPayment").addClass("d-none");
        $("#formPurchase").removeClass("d-none");
    } else if ($("#voucherType").val() == "PAYMENT" || $("#voucherType").val() == "RECEIPT") {
        $("#formPayment").removeClass("d-none");
        $("#formPurchase").addClass("d-none");
    }
}

function changeAncAttSess() {
    $('#to').attr({ min: $('#from').val() });
    if ($('#from').val() && $('#to').val()) {
        $('#stockReport').removeClass("disabled");
    }
    let url = '/stockReport?' + $.param({
        from: $('#from').val(),
        to: $('#to').val()
    });
    $('#stockReport').attr({ target: '_blank', href: url });
}

function changeAccPeriod() {
    $('#to').attr({ min: $('#from').val() });
    $('#to').attr({ max: $('#from').attr('max') });
    if ($('#from').val() && $('#to').val()) {
        $('#accDetailButton').removeAttr("disabled");
    } else {
        $('#accDetailButton').attr("disabled", "disabled");
    }
}

function fetchvehicleDetail(id) {

    $.ajax({
        type: "GET",
        url: '/vehicleDetails?' + $.param({
            id: id
        }),
        success: function (data) {
            // var json = $.parseJSON(data);
            $("#registrationNumber").html(data.registrationNumber);
            $("#customerName").html(data.customerName);
            $("#assetDesc").html(data.assetDesc);
            $("#agent").html(data.agent);
            $("#currentStatus").html(data.status);

            let url = "/changeStatus?id=" + data.id;
            $('.change').attr({ href: url });
        },
        contentType: "application/json"
    });
}

function bgcolor(status) {
    if (status == "RED") { return "bg-danger"; }
    else if (status == "GREEN") { return "bg-success"; }
}
$(document).ready(function () {

    $.ajax({
        type: "GET",
        url: '/markedVehicles',
        success: function (data) {
            var trHTML = '';
            for (let i = 0; i < data.length; i++) {
                if (i % 2 == 0) {
                    trHTML += '<tr>';
                    trHTML += '<td class=' + bgcolor(data[i].status) + '><span data-toggle="modal" data-target="#changeStatusModal"><a href="#" data-toggle="tooltip" data-placement="top" title="Archive Student" class="px-2 text-dark font-weight-bold" id="' + data[i].id + '" onclick="fetchvehicleDetail(this.id);">' + data[i].registrationNumber + '</td>';
                    if (i < data.length - 1) {
                        trHTML += '<td class=' + bgcolor(data[i + 1].status) + '><span data-toggle="modal" data-target="#changeStatusModal"><a href="#" data-toggle="tooltip" data-placement="top" title="Archive Student" class="px-2 text-dark font-weight-bold" id="' + data[i + 1].id + '" onclick="fetchvehicleDetail(this.id);">' + data[i + 1].registrationNumber + '</td>';
                    }
                    trHTML += '</tr>';
                }
            }
            $('#studentTable').html(trHTML);
            $('#studentSearch').val('');
        },
        contentType: "application/json"
    });

    $("#formPayment").hide();
    
    $('#delete').click(function (e) {

        e.preventDefault();
        $('#delete').off('click');

        $.ajax({
            type: "GET",
            url: "/deleteAll",
            success: function (data) {
                $('#msg').html(data);
            },
            contentType: "application/json"
        });

    });


    // Listen to click event on the submit button
    $('#button').click(function (e) {

        e.preventDefault();

        let vendor = {
            name: $("#name").val(),
            mobile: $("#mobile").val(),
            email: $("#email").val(),
            aadhaar: $("#aadhaar").val(),
            openingBalance: $("#openingBalance").val(),
            address1: $("#address1").val()
        };

        $.ajax({
            type: "POST",
            url: "/create",
            data: JSON.stringify(vendor),
            success: function (data) {
                $("#msg").show();
                $('#msg').html(data);
                $('html,body').animate({
                    scrollTop: $("#msg").offset().top
                }, 'slow');
                $('.clearit').val('');
                setTimeout(function () {
                    $("#msg").hide();
                }, 3000);
            },
            contentType: "application/json"
        });
    });

    $('#whButton').click(function (e) {

        e.preventDefault();

        let warehouse = {
            name: $("#whName").val(),
            location: $("#location").val()
        };

        $.ajax({
            type: "POST",
            url: "/createWarehouse",
            data: JSON.stringify(warehouse),
            success: function (data) {
                $("#msg").show();
                $('#msg').html(data);
                $('html,body').animate({
                    scrollTop: $("#msg").offset().top
                }, 'slow');
                $('.clearit').val('');
                setTimeout(function () {
                    $("#msg").hide();
                }, 3000);
            },
            contentType: "application/json"
        });
    });

    $('#pdButton').click(function (e) {

        e.preventDefault();
        let openingStocks = [];
        $('.stock').each(function (i, item) {
            let whName = $(item).attr('id').substring(5);
            let stockVal = $(item).val();
            let stock = {
                wareHouseName: whName,
                openingStock: stockVal
            };
            openingStocks.push(stock);
        })
        let product = {
            name: $("#pdName").val(),
            manufacturer: $("#manufacturer").val(),
            openingStocks: openingStocks
        };

        $.ajax({
            type: "POST",
            url: "/createProduct",
            data: JSON.stringify(product),
            success: function (data) {
                $("#msg").show();
                $('#msg').html(data);
                $('html,body').animate({
                    scrollTop: $("#msg").offset().top
                }, 'slow');
                $('.clearit').val('');
                setTimeout(function () {
                    $("#msg").hide();
                }, 3000);
            },
            contentType: "application/json"
        });
    });

    $('#idSearch').click(function (e) {
        e.preventDefault();
        accountDetailAjax();
    });

    $('#productIdSearch').click(function (e) {
        console.log("here");
        e.preventDefault();
        $.ajax({
            type: "GET",
            url: '/stockDetails?' + $.param({
                id: $('#id').val()
            }),
            success: function (data) {
                $('#stockDetail').html(data);
                $(".convertTime").each(function () {
                    let utcDate = $(this).text();
                    let localDate = new Date(utcDate);
                    let x = localDate.toLocaleDateString('en-GB', {
                        day: '2-digit', month: 'short', year: 'numeric'
                    }).replace(/ /g, '-');
                    $(this).text(x);
                });
                $('.data-row').each(function () {
                    let voucherType = $(this).find(".vType").text();
                    if (voucherType == "PURCHASE" || voucherType == "RECEIPT") {
                        // $(this).addClass("table-danger");
                    } else if (voucherType == "PAYMENT" || voucherType == "SALE") {
                        // $(this).addClass("table-success");
                    }
                });
            },
            contentType: "application/json"
        });
    });

    $("#studentSearch").on("keyup", function () {
        let value = $(this).val().toLowerCase();
        if (value.length == 4) {
            $.ajax({
                type: "GET",
                url: '/vehicleQuery?' + $.param({
                    q: value
                }),
                success: function (data) {
                    var trHTML = '';
                    for (let i = 0; i < data.length; i++) {
                        if (i % 2 == 0) {
                            trHTML += '<tr>';
                            trHTML += '<td class=' + bgcolor(data[i].status) + '><span data-toggle="modal" data-target="#changeStatusModal"><a href="#" data-toggle="tooltip" data-placement="top" title="Archive Student" class="px-2 text-dark font-weight-bold" id="' + data[i].id + '" onclick="fetchvehicleDetail(this.id);">' + data[i].registrationNumber + '</td>';
                            if (i < data.length - 1) {
                                trHTML += '<td class=' + bgcolor(data[i + 1].status) + '><span data-toggle="modal" data-target="#changeStatusModal"><a href="#" data-toggle="tooltip" data-placement="top" title="Archive Student" class="px-2 text-dark font-weight-bold" id="' + data[i + 1].id + '" onclick="fetchvehicleDetail(this.id);">' + data[i + 1].registrationNumber + '</td>';
                            }
                            trHTML += '</tr>';
                        }
                    }
                    $('#studentTable').html(trHTML);
                    $('#studentSearch').val('');
                },
                contentType: "application/json"
            });
        }
    });

    $("body").on('show.bs.modal', "#changeStatusModal", function (event) {
        $('#reverseMsg').hide();
        let button = $(event.relatedTarget); // Button that triggered the modal
        let id = button.data('id'); // Extract info from data-* attributes
        let url = "/changeStatus?registrationNumber=" + id;

        let modal = $(this);
        modal.find('.change').attr({ href: url });
    });

    $("body").on('click', "#changeStatus", function (event) {
        event.preventDefault();
        let url = $(this).attr('href');
        let status = $('#status').val();
        url = url + "&status=" + status;
        $.ajax({
            type: "GET",
            url: url,
            success: function (data) {
                $("#statusMsg").show();
                $('#statusMsg').html(data);
                $('.clearit').val('');
                setTimeout(function () {
                    $("#statusMsg").hide();
                    $('#changeStatusModal').modal('hide');
                    $('.modal-backdrop').remove();
                }, 1000);
            }
        });
    });


    $('#customFile').on('change', function () {
        //get the file name
        var fileName = $(this).val();
        //replace the "Choose a file" label
        $(this).next('.custom-file-label').html(fileName);
    });

    $('#upload').on('click', function (e) {
        e.preventDefault();
        let file = $('#customFile')[0].files[0];
        let upload = new Upload(file);
        upload.doUpload();
        // maybe check size or type here with upload.getSize() and upload.getType()
        // if( upload.getType() == "text/csv" ) {  // only allow csv
        // execute upload

        // }
        // else {
        // alert('Only CSV files are allowed');
        // }
    });

    $("#product").on("keyup", function () {
        let value = $(this).val().toLowerCase();
        $(".product-value").filter(function () {
            $(this).toggle($(this).text().toLowerCase().indexOf(value) > -1)
        });
    });

    $("body").on('keyup', ".price", function () {
        $('#value').attr('value', function () {
            let quantity = $('#quantity').val()
            let rate = $('#rate').val();
            return quantity * rate;
        });
    });

    $("body").on('click', "#submitVoucher", function () {
        let voucher = new Object();
        console.log($('#voucherId').val());
        if ($('#voucherId').val() != '')
            return;
        voucher.voucherType = $("#voucherType").val();
        voucher.transactionDate = $("#transactionDate").val();
        if (voucher.voucherType == "PURCHASE" || voucher.voucherType == "SALE") {
            voucher.productId = $("#product").val();
            voucher.warehouseId = $("#warehouse").val();
            voucher.unit = $("#unit").val();
            voucher.quantity = $("#quantity").val();
            voucher.transactionInfo = $("#transactionInfo").val();
            voucher.rate = $("#rate").val();
            voucher.value = $("#value").val();
        } else if (voucher.voucherType == "PAYMENT" || voucher.voucherType == "RECEIPT") {
            voucher.mode = $("#mode").val();
            voucher.transactionInfo = $("#transactionId").val();
            voucher.value = $("#amount").val();
        }
        $.ajax({
            type: "POST",
            url: "/createVoucher?" + $.param({
                id: $('#id').val()
            }),
            data: JSON.stringify(voucher),
            success: function (data) {
                $("#msg").show();
                $('#msg').html(data);
                $('.clearit').val('');
                setTimeout(function () {
                    $("#msg").hide();
                    $('#exampleModalCenter').modal('hide');
                    $('.modal-backdrop').remove();
                    $("#idSearch").click();
                }, 3000);
            },
            contentType: "application/json"
        });
    });

    $("body").on('show.bs.modal', "#exampleModalCenter", function (event) {
        // Note: modal converts transactionInfo --> transactioninfo (all lower case)
        $('#msg').hide();
        setFstDropdown();
        let today = new Date();
        let todayStr = today.toISOString().substring(0, 10);
        $('#transactionDate').attr({ max: todayStr });


        let trig = $(event.relatedTarget);


        $('#exampleModalLongTitle').text(trig.data('action'));
        if (trig.data('action') != 'Edit Voucher') {
            return;
        }

        $('#voucherType').val(trig.data('vouchertype'));
        $('#voucherType').trigger("change");

        $('.product .fstlist').find('div[data-value="001"]').removeClass("selected");
        let prodSelector = $('.product .fstlist').find('div[data-value="' + trig.data('id') + '"]');
        prodSelector.addClass("selected");
        $('.product .fstselected').text(prodSelector.text());

        $('.warehouse .fstlist').find('div[data-value="001"]').removeClass("selected");
        let whSelector = $('.warehouse .fstlist').find('div[data-value="' + trig.data('warehouse') + '"]');
        whSelector.addClass("selected");
        $('.warehouse .fstselected').text(whSelector.text());

        setFstDropdown();
        $('#transactionDate').val(trig.data('transactiondate').substring(0, 10));
        $('#transactionInfo').val(trig.data('transactioninfo'));
        $('#quantity').val(trig.data('quantity'));
        $('#rate').val(trig.data('rate'));
        $('#value').val(trig.data('value'));
        $('#amount').val(trig.data('value'));
        $('#mode').val(trig.data('mode'));
        $('#voucherId').text(trig.data('voucherid'));
    });

});
